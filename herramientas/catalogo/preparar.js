// Prepara el catálogo de Pixabay para incluirlo en la app (T-71). Uso:
//   1. Poner las imágenes elegidas en la carpeta imagenes/ y anotarlas en catalogo.json
//      (id, título, archivo, autor y enlace de Pixabay). Anotarlas también en LICENCIAS.md.
//   2. npm install && npm run preparar
// Cada imagen se recorta al centro, queda en 1080x1080 y se comprime hasta pesar máximo 500 KB
// (RNF-06). Se guarda en app/src/main/assets/catalogo/ y se agrega a incluidas.json.
// Plan Spark: no hay Cloud Storage, así que las imágenes viajan dentro de la app.
const fs = require('fs');
const path = require('path');
const sharp = require('sharp');

const assets = path.join(__dirname, '..', '..', 'app', 'src', 'main', 'assets', 'catalogo');
const listado = path.join(assets, 'incluidas.json');

async function preparar(archivo) {
  for (let calidad = 85; calidad >= 40; calidad -= 5) {
    const datos = await sharp(archivo).resize(1080, 1080, { fit: 'cover' }).jpeg({ quality: calidad }).toBuffer();
    if (datos.length <= 500 * 1024) return datos;
  }
  throw new Error(`${archivo} no baja de 500 KB`);
}

(async () => {
  const lista = JSON.parse(fs.readFileSync(path.join(__dirname, 'catalogo.json'), 'utf8'))
    .filter((img) => !img.id.startsWith('ejemplo'));
  const incluidas = JSON.parse(fs.readFileSync(listado, 'utf8'));
  for (const img of lista.sort((a, b) => a.orden - b.orden)) {
    const datos = await preparar(path.join(__dirname, 'imagenes', img.archivo));
    const archivo = `${img.id}.jpg`;
    fs.writeFileSync(path.join(assets, archivo), datos);
    const entrada = {
      id: img.id, titulo: img.titulo, archivo, autor: img.autor, origen: img.origen,
      licencia: 'Pixabay Content License',
    };
    const i = incluidas.findIndex((x) => x.id === img.id);
    if (i >= 0) incluidas[i] = entrada; else incluidas.push(entrada);
    console.log(`lista ${img.id} (${Math.round(datos.length / 1024)} KB)`);
  }
  fs.writeFileSync(listado, '[\n' + incluidas.map((x) => '  ' + JSON.stringify(x)).join(',\n') + '\n]\n');
  console.log(`incluidas.json: ${incluidas.length} imágenes`);
})().catch((e) => { console.error(e.message); process.exit(1); });
