TRABAJO REALIZADO POR: 
- BPAVEZ12 = BENJAMIN PAVEZ
- CQUIROZ1404 = CRISTHIAN QUIROZ
- MSWINDOW87 = VICTOR UGALDE

Este es un prototipo de aplicación de comercio electrónico (E-commerce) desarrollado a partir de una versión simplificada de la idea original (Marketplace), con el objetivo de cumplir los requisitos de la evaluación. Para ello, se implementó una interfaz sencilla y una base de datos local para gestionar productos y perfiles de usuario.Por lo que hasta el momento esta es la primera version pero tenemos muchas mas ideas para implementar


-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# :shopping_cart:Marketplace:shopping_cart: 
E-commerce es una aplicacion desarollada en el entorno de android studio y consiste en un administrador virtual de productos.Esta aplicacion incluye la posibilidad al usuario de crear su perfil y editarlo o personalizarlo a su gusto ademas de poder ver los productos disponibles en la aplicacion y sus detalles,  en el caso de que sea el autor de la publicacion del producto podra editar el producto ya sea la descripcion nombre, foto, cantidad o directamente eliminarlo si desea.

## 📁 Estructura del Proyecto

```bash
📦 java/com/example/appconsqlite/
│
├── 📂 ui/
│   ├── 📂 auth/
│   │   ├── 🧩 MainActivity.java         (Tu pantalla de Login)
│   │   └── 🧩 Registro.java
│   │
│   ├── 📂 main/
│   │   └── 🧩 MenuActivity.java
│   │
│   ├── 📂 product/
│   │   ├── 🧩 AgregarProductoActivity.java
│   │   ├── 🧩 DetalleProductoActivity.java
│   │   └── 🧩 EditarProductoActivity.java
│   │
│   └── 📂 profile/
│       ├── 🧩 PerfilActivity.java
│       └── 🧩 EditarPerfilActivity.java
│
├── 📂 data/
│   ├── 📂 db/
│   │   ├── 🧩 DBHelper.java
│   │   └── 🧩 ProductContract.java
│   │
│   └── 📂 repository/
│       └── 🧩 ProductRepository.java
│
└── 📂 adapters/
    └── 🧩 ProductAdapter.java
```
---------------------------------------------------------------------------------------------

## 📊Base de datos

<img src="https://media.discordapp.net/attachments/678413432877482007/1429656662742007929/image.png?ex=68f6ef06&is=68f59d86&hm=62e281e1ac45b2a2b653f3cec7357236065db2151daa303775eb11a820fd7758&=&format=webp&quality=lossless&width=950&height=411" width="800"/>

- DBHelper.java: se almacenan los planos de la base de datos, definen las tablas y columnas
- UserContract.java: centraliza y estandariza la estructura de la tabla Usuario
- ProductContract.java: centraliza y estandariza la estructura de la tabla Productos
---------------------------------------------------------------------------------------------
## 🧪Pruebas
Se realizaron las pruebas en un teléfono Google Pixel 6 Pro con la API 31.

<img src="https://i.blogs.es/1a2f94/google-pixel-6-pro_1/840_560.jpg" width="800"/>
