MS-PRODUCT 

GET http://localhost:8088/api/product
response
 {
    "categoria": "Tecnologia",
    "descripcion": null,
    "fechaCreacion": null,
    "id": "1",
    "nombre": "Celular",
    "precio": 2500.69,
    "stock": null
  }

POST http://localhost:8088/api/product
request body
{
  "nombre": "Laptop Lenovo ThinkPad",
  "descripcion": "Laptop empresarial con 16GB RAM",
  "precio": 1200.5,
  "categoria": "Tecnología",
  "stock": 10
}

MS-CUSTOMER

POST http://localhost:8085/api/customers
Response
{
    "apellido": "Pérez",
    "correo": "juan.perez@mail.com",
    "dni": "12345678",
    "id": 1,
    "nombre": "Juan"
}


