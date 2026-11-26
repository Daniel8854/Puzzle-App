// Pruebas de las reglas con el emulador (T-14). Se ejecutan con: npm test
const { test, before, beforeEach, after } = require('node:test');
const fs = require('node:fs');
const path = require('node:path');
const {
  initializeTestEnvironment, assertFails, assertSucceeds,
} = require('@firebase/rules-unit-testing');
const { doc, getDoc, setDoc, updateDoc, writeBatch, serverTimestamp } = require('firebase/firestore');

let entorno;
const raiz = path.join(__dirname, '..');

before(async () => {
  entorno = await initializeTestEnvironment({
    projectId: 'demo-puzzle',
    firestore: { rules: fs.readFileSync(path.join(raiz, 'firestore.rules'), 'utf8'), host: '127.0.0.1', port: 8080 },
  });
});
beforeEach(async () => {
  await entorno.clearFirestore();
  // Datos de partida creados sin reglas: dos jugadores ya registrados.
  await entorno.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await setDoc(doc(db, 'usuarios/bob'), { nombre: 'Bob', nombreMinusculas: 'bob', avatar: 1 });
    await setDoc(doc(db, 'nombres/bob'), { uid: 'bob' });
    await setDoc(doc(db, 'usuarios/bob/privado/datos'), { correo: 'bob@correo.com' });
  });
});
after(async () => { await entorno.cleanup(); });

const alicia = () => entorno.authenticatedContext('alicia').firestore();
const bob = () => entorno.authenticatedContext('bob').firestore();
const carlos = () => entorno.authenticatedContext('carlos').firestore();
const invitado = () => entorno.unauthenticatedContext().firestore();

function registrar(db, uid, nombre) {
  const lote = writeBatch(db);
  lote.set(doc(db, `nombres/${nombre.toLowerCase()}`), { uid });
  lote.set(doc(db, `usuarios/${uid}`), {
    nombre, nombreMinusculas: nombre.toLowerCase(), avatar: 0, creadoEn: serverTimestamp(),
  });
  return lote.commit();
}

// ---------- Registro y datos privados ----------

test('un jugador crea su perfil reservando un nombre libre', async () => {
  await assertSucceeds(registrar(alicia(), 'alicia', 'Alicia'));
});

test('no se puede registrar un nombre ya usado', async () => {
  await assertFails(registrar(alicia(), 'alicia', 'BOB'));
});

test('no se puede crear el perfil de otro', async () => {
  await assertFails(setDoc(doc(alicia(), 'usuarios/carlos'), { nombre: 'Carlos', nombreMinusculas: 'carlos', avatar: 0 }));
});

test('el cliente solo cambia su avatar: no se pone trofeos ni campos inventados (RNF-16)', async () => {
  await assertFails(updateDoc(doc(bob(), 'usuarios/bob'), { codigoAmigo: 'ABCDEFGH' }));
  await assertFails(updateDoc(doc(bob(), 'usuarios/bob'), { trofeos: 9999 }));
  await assertSucceeds(updateDoc(doc(bob(), 'usuarios/bob'), { avatar: 3 }));
});

test('cada uno solo lee sus datos privados', async () => {
  await assertSucceeds(getDoc(doc(bob(), 'usuarios/bob/privado/datos')));
  await assertFails(getDoc(doc(alicia(), 'usuarios/bob/privado/datos')));
  await assertFails(setDoc(doc(alicia(), 'usuarios/bob/privado/datos'), { correo: 'x' }));
});

test('un invitado no lee ni escribe nada en la nube', async () => {
  await assertFails(getDoc(doc(invitado(), 'usuarios/bob')));
  await assertFails(setDoc(doc(invitado(), 'usuarios/x'), { nombre: 'Xxx' }));
});

test('puntajes, rankings, amistades y códigos sueltos no se escriben desde el cliente', async () => {
  await assertFails(setDoc(doc(bob(), 'ranking/temporada1'), { bob: 100 }));
  await assertFails(setDoc(doc(bob(), 'duelos/d1'), { retador: 'bob', retado: 'alicia', ganador: 'bob' }));
  await assertFails(setDoc(doc(bob(), 'usuarios/bob/amigos/alicia'), { desde: 1 }));
  await assertFails(setDoc(doc(bob(), 'codigos/ABCDEFGH'), { uid: 'bob' }));
});

// ---------- Código de amigo (T-51) ----------

function reservarCodigo(db, uid, codigo, para = uid) {
  const lote = writeBatch(db);
  lote.set(doc(db, `codigos/${codigo}`), { uid: para });
  lote.update(doc(db, `usuarios/${uid}`), { codigo });
  return lote.commit();
}

test('un jugador reserva un código libre y lo copia a su perfil', async () => {
  await assertSucceeds(reservarCodigo(bob(), 'bob', 'ABCDEFGH'));
  await assertSucceeds(getDoc(doc(alicia(), 'codigos/ABCDEFGH')));
});

test('el código no se cambia después ni se reserva uno ocupado', async () => {
  await reservarCodigo(bob(), 'bob', 'ABCDEFGH');
  await assertFails(reservarCodigo(bob(), 'bob', 'HGFEDCBA'));
  await registrar(alicia(), 'alicia', 'Alicia');
  await assertFails(reservarCodigo(alicia(), 'alicia', 'ABCDEFGH'));
});

test('el código tiene 8 caracteres sin 0, O, 1 ni I, y no se reserva para otro', async () => {
  await assertFails(reservarCodigo(bob(), 'bob', 'ABCDEFG0'));
  await assertFails(reservarCodigo(bob(), 'bob', 'ABCDEFGHJ'));
  await assertFails(reservarCodigo(bob(), 'bob', 'ABCDEFGH', 'alicia'));
});

// ---------- Solicitudes de amistad ----------

test('se envía una solicitud válida y solo la ven sus dos jugadores', async () => {
  await registrar(alicia(), 'alicia', 'Alicia');
  await assertSucceeds(setDoc(doc(alicia(), 'solicitudes/alicia_bob'),
    { de: 'alicia', para: 'bob', estado: 'pendiente', creadaEn: serverTimestamp() }));
  await assertSucceeds(getDoc(doc(bob(), 'solicitudes/alicia_bob')));
  await assertFails(getDoc(doc(carlos(), 'solicitudes/alicia_bob')));
});

test('no se envía una solicitud a uno mismo ni a nombre de otro', async () => {
  await assertFails(setDoc(doc(bob(), 'solicitudes/bob_bob'), { de: 'bob', para: 'bob', estado: 'pendiente' }));
  await assertFails(setDoc(doc(alicia(), 'solicitudes/bob_alicia'), { de: 'bob', para: 'alicia', estado: 'pendiente' }));
});

test('no se envía una solicitud a un amigo actual', async () => {
  await entorno.withSecurityRulesDisabled((ctx) =>
    setDoc(doc(ctx.firestore(), 'usuarios/alicia/amigos/bob'), { desde: 1 }));
  await assertFails(setDoc(doc(alicia(), 'solicitudes/alicia_bob'), { de: 'alicia', para: 'bob', estado: 'pendiente' }));
});

test('solo quien la recibe la acepta o la rechaza', async () => {
  await entorno.withSecurityRulesDisabled((ctx) =>
    setDoc(doc(ctx.firestore(), 'solicitudes/alicia_bob'), { de: 'alicia', para: 'bob', estado: 'pendiente' }));
  await assertFails(updateDoc(doc(alicia(), 'solicitudes/alicia_bob'), { estado: 'aceptada' }));
  await assertSucceeds(updateDoc(doc(bob(), 'solicitudes/alicia_bob'), { estado: 'aceptada' }));
});

// ---------- Amistades (T-53) ----------

async function solicitudPendiente() {
  await entorno.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await setDoc(doc(db, 'usuarios/alicia'), { nombre: 'Alicia', nombreMinusculas: 'alicia', avatar: 2 });
    await setDoc(doc(db, 'solicitudes/alicia_bob'), { de: 'alicia', para: 'bob', estado: 'pendiente' });
  });
}

function aceptar(db, estado = 'aceptada') {
  const lote = writeBatch(db);
  lote.update(doc(db, 'solicitudes/alicia_bob'), { estado });
  lote.set(doc(db, 'usuarios/bob/amigos/alicia'), { nombre: 'Alicia', avatar: 2, desde: serverTimestamp() });
  lote.set(doc(db, 'usuarios/alicia/amigos/bob'), { nombre: 'Bob', avatar: 1, desde: serverTimestamp() });
  return lote.commit();
}

test('al aceptar, quien recibe la solicitud crea la amistad en las dos cuentas', async () => {
  await solicitudPendiente();
  await assertSucceeds(aceptar(bob()));
  await assertSucceeds(getDoc(doc(alicia(), 'usuarios/alicia/amigos/bob')));
  await assertFails(getDoc(doc(carlos(), 'usuarios/alicia/amigos/bob')));
});

test('no se crea una amistad sin solicitud aceptada ni la crea un tercero', async () => {
  await solicitudPendiente();
  await assertFails(aceptar(bob(), 'rechazada'));
  await assertFails(aceptar(alicia()));
  await assertFails(setDoc(doc(carlos(), 'usuarios/bob/amigos/alicia'), { nombre: 'X', avatar: 0, desde: serverTimestamp() }));
});
