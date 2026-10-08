"""Builds the 3D Butit Kulit book (unity/.../StreamingAssets/models/book/butit-kulit.glb).

The book lies flat, laid out in Unity's axes: x to the right, y up, z towards the top of the book,
spine on the left, read from -z. glTF is right-handed and glTFast mirrors x when it imports, so the
file stores x mirrored (see add_mesh / add_node) and the book comes out the right way round in Unity.
Nodes the Unity bridge uses by name:
  CoverPivot   hinge on the spine; rotate it 180 degrees about z to open the book
  Page3..Page10  the right-hand page for each printed story page (show one at a time)
  SceneAnchor  where a page's 3D scene stands when the book is open

Pictures come from the Butit Kulit PDF (story and drawings by Joan D. Mayo, DepEd Manila):
pass the folder of page renders made with `pdftoppm -r 110 -jpeg Butit-Kulit.pdf p`.
Run: python3 tools/make-book-glb.py <pages-folder> <out.glb>
"""
import io
import json
import struct
import sys

from PIL import Image, ImageDraw, ImageFont

PAGES, OUT = sys.argv[1], sys.argv[2]
W, H = 0.15, 0.212           # page size in meters (A4 proportions)
BACK, BLOCK, COVER = 0.002, 0.012, 0.002
SPINE = BACK + BLOCK + COVER

bin_parts, buffer_views, accessors, images, textures, materials, meshes, nodes = [], [], [], [], [], [], [], []
offset = 0


def add_bytes(data, target=None):
    global offset
    pad = (-offset) % 4
    if pad:
        bin_parts.append(b'\0' * pad)
        offset += pad
    view = {'buffer': 0, 'byteOffset': offset, 'byteLength': len(data)}
    if target:
        view['target'] = target
    buffer_views.append(view)
    bin_parts.append(data)
    offset += len(data)
    return len(buffer_views) - 1


def add_accessor(values, kind, comp=5126):
    n = {'SCALAR': 1, 'VEC2': 2, 'VEC3': 3}[kind]
    fmt = '<' + ('H' if comp == 5123 else 'f') * n
    data = b''.join(struct.pack(fmt, *(v if n > 1 else (v,))) for v in values)
    acc = {'bufferView': add_bytes(data, 34963 if comp == 5123 else 34962), 'componentType': comp, 'count': len(values), 'type': kind}
    if kind == 'VEC3':
        acc['min'] = [min(v[i] for v in values) for i in range(3)]
        acc['max'] = [max(v[i] for v in values) for i in range(3)]
    accessors.append(acc)
    return len(accessors) - 1


def add_image(im, quality=82):
    buf = io.BytesIO()
    im.convert('RGB').save(buf, 'JPEG', quality=quality)
    images.append({'bufferView': add_bytes(buf.getvalue()), 'mimeType': 'image/jpeg'})
    textures.append({'source': len(images) - 1, 'sampler': 0})
    return len(textures) - 1


def add_material(name, color=(1, 1, 1), texture=None):
    pbr = {'baseColorFactor': [*color, 1.0], 'metallicFactor': 0.0, 'roughnessFactor': 1.0}
    if texture is not None:
        pbr['baseColorTexture'] = {'index': texture}
    materials.append({'name': name, 'pbrMetallicRoughness': pbr, 'extensions': {'KHR_materials_unlit': {}}})
    return len(materials) - 1


def quad(x0, x1, z0, z1, y, up=True, uv=((0, 1), (1, 1), (1, 0), (0, 0))):
    """A flat rectangle at height y. The image's top edge is at z1 (the top of the book)."""
    p = [(x0, y, z0), (x1, y, z0), (x1, y, z1), (x0, y, z1)]
    n = [(0, 1 if up else -1, 0)] * 4
    i = [0, 2, 1, 0, 3, 2] if up else [0, 1, 2, 0, 2, 3]
    return p, n, list(uv), i


def box(x0, x1, y0, y1, z0, z1):
    faces = [  # (normal, corners)
        ((0, 1, 0), [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)]),
        ((0, -1, 0), [(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)]),
        ((1, 0, 0), [(x1, y0, z0), (x1, y0, z1), (x1, y1, z1), (x1, y1, z0)]),
        ((-1, 0, 0), [(x0, y0, z1), (x0, y0, z0), (x0, y1, z0), (x0, y1, z1)]),
        ((0, 0, 1), [(x1, y0, z1), (x0, y0, z1), (x0, y1, z1), (x1, y1, z1)]),
        ((0, 0, -1), [(x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0)]),
    ]
    p, n, uv, idx = [], [], [], []
    for normal, corners in faces:
        b = len(p)
        p += corners
        n += [normal] * 4
        uv += [(0, 1), (1, 1), (1, 0), (0, 0)]
        idx += [b, b + 2, b + 1, b, b + 3, b + 2]
    return p, n, uv, idx


def add_mesh(name, geometry, material):
    p, n, uv, idx = geometry
    p = [(-x, y, z) for x, y, z in p]
    n = [(-x, y, z) for x, y, z in n]
    idx = [v for t in range(0, len(idx), 3) for v in (idx[t], idx[t + 2], idx[t + 1])]  # mirroring flips the winding
    meshes.append({'name': name, 'primitives': [{
        'attributes': {'POSITION': add_accessor(p, 'VEC3'), 'NORMAL': add_accessor(n, 'VEC3'), 'TEXCOORD_0': add_accessor(uv, 'VEC2')},
        'indices': add_accessor(idx, 'SCALAR', 5123), 'material': material}]})
    return len(meshes) - 1


def add_node(name, mesh=None, children=(), translation=None):
    node = {'name': name}
    if mesh is not None:
        node['mesh'] = mesh
    if children:
        node['children'] = list(children)
    if translation:
        node['translation'] = [-translation[0], translation[1], translation[2]]
    nodes.append(node)
    return len(nodes) - 1


def page(n):
    return Image.open(f'{PAGES}/p-{n:02d}.jpg')


def font(size):
    for f in ['/System/Library/Fonts/Supplemental/Arial Bold.ttf', '/Library/Fonts/Arial Bold.ttf', '/System/Library/Fonts/Helvetica.ttc']:
        try:
            return ImageFont.truetype(f, size)
        except OSError:
            pass
    return ImageFont.load_default()


# ---- pictures ----
def shrink(im, width=720):
    return im.resize((width, round(im.height * width / im.width)), Image.LANCZOS)


cover_tex = add_image(shrink(page(1)))
inside_tex = add_image(shrink(page(2)))
back = Image.new('RGB', (720, 1018), (251, 250, 246))
d = ImageDraw.Draw(back)
d.text((360, 380), 'Butit Kulit', font=font(72), fill=(30, 30, 30), anchor='mm')
d.text((360, 470), 'Kuwento at iginuhit ni Joan D. Mayo', font=font(30), fill=(90, 90, 90), anchor='mm')
d.rounded_rectangle((180, 540, 540, 556), radius=8, fill=(63, 154, 58))
d.text((360, 900), 'DepEd Manila · Hindi ipinagbibili', font=font(26), fill=(47, 125, 44), anchor='mm')
back_tex = add_image(back)
spine = Image.new('RGB', (1018, 110), (247, 245, 238))
d = ImageDraw.Draw(spine)
d.text((509, 55), 'Butit Kulit  ·  Joan D. Mayo', font=font(54), fill=(31, 31, 31), anchor='mm')
d.rectangle((900, 0, 1018, 110), fill=(63, 143, 58))
spine_tex = add_image(spine)
edge = Image.new('RGB', (64, 64), (239, 234, 220))
for y in range(0, 64, 3):
    ImageDraw.Draw(edge).line((0, y, 64, y), fill=(217, 210, 191))
edge_tex = add_image(edge)

cover_m = add_material('Cover', texture=cover_tex)
inside_m = add_material('Inside', texture=inside_tex)
back_m = add_material('Back', texture=back_tex)
spine_m = add_material('Spine', texture=spine_tex)
edge_m = add_material('Pages', texture=edge_tex)
board_m = add_material('Board', color=(0.97, 0.96, 0.93))

# ---- geometry (centred on the book) ----
x0, x1, z0, z1 = -W / 2, W / 2, -H / 2, H / 2
children = []
children.append(add_node('BackCover', add_mesh('BackCover', box(x0, x1, 0, BACK, z0, z1), board_m)))
children.append(add_node('BackPicture', add_mesh('BackPicture', quad(x0, x1, z0, z1, -0.0002, up=False, uv=((1, 1), (0, 1), (0, 0), (1, 0))), back_m)))
children.append(add_node('PageBlock', add_mesh('PageBlock', box(x0 + 0.002, x1 - 0.002, BACK, BACK + BLOCK, z0 + 0.003, z1 - 0.003), edge_m)))
# spine: a strip on the left edge, facing left
sp = [(x0 - 0.0005, 0, z0), (x0 - 0.0005, 0, z1), (x0 - 0.0005, SPINE, z1), (x0 - 0.0005, SPINE, z0)]
children.append(add_node('Spine', add_mesh('Spine', (sp, [(-1, 0, 0)] * 4, [(1, 1), (0, 1), (0, 0), (1, 0)], [0, 1, 2, 0, 2, 3]), spine_m)))
# the right-hand page for each story page, on top of the page block; only one is shown at a time
top = BACK + BLOCK + 0.0003
for printed in range(3, 11):
    tex = add_image(shrink(page(printed + 1)))  # PDF page 4 is printed page 3
    m = add_material(f'Page{printed}', texture=tex)
    children.append(add_node(f'Page{printed}', add_mesh(f'Page{printed}', quad(x0 + 0.003, x1 - 0.003, z0 + 0.004, z1 - 0.004, top), m)))
# front cover on a hinge at the spine: the picture on top, the title page on the inside
cover_parts = [
    add_node('CoverBoard', add_mesh('CoverBoard', box(0, W, 0, COVER, z0, z1), board_m)),
    add_node('CoverPicture', add_mesh('CoverPicture', quad(0, W, z0, z1, COVER + 0.0002), cover_m)),
    # seen from above once the cover has turned over to the left (mirrored so it reads correctly)
    add_node('CoverInside', add_mesh('CoverInside', quad(0, W, z0, z1, -0.0002, up=False, uv=((1, 1), (0, 1), (0, 0), (1, 0))), inside_m)),
]
children.append(add_node('CoverPivot', children=cover_parts, translation=(x0, BACK + BLOCK, 0)))
children.append(add_node('SceneAnchor', translation=(0, top, 0)))
root = add_node('ButitKulitBook', children=children)

binary = b''.join(bin_parts)
binary += b'\0' * ((-len(binary)) % 4)
gltf = {
    'asset': {'version': '2.0', 'generator': 'BASA-KITA make-book-glb.py'},
    'extensionsUsed': ['KHR_materials_unlit'],
    'scene': 0, 'scenes': [{'nodes': [root]}], 'nodes': nodes, 'meshes': meshes, 'materials': materials,
    'textures': textures, 'images': images, 'samplers': [{'magFilter': 9729, 'minFilter': 9987, 'wrapS': 33071, 'wrapT': 33071}],
    'accessors': accessors, 'bufferViews': buffer_views, 'buffers': [{'byteLength': len(binary)}],
}
js = json.dumps(gltf, separators=(',', ':')).encode()
js += b' ' * ((-len(js)) % 4)
with open(OUT, 'wb') as f:
    f.write(b'glTF' + struct.pack('<II', 2, 28 + len(js) + len(binary)))
    f.write(struct.pack('<I', len(js)) + b'JSON' + js + struct.pack('<I', len(binary)) + b'BIN\0' + binary)
print(OUT, len(js) + len(binary), 'bytes')
