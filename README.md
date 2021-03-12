# API user :pencil:

API user es un API rest maneja una base de datos SQL para realizar operaciones básicas como crear, eliminar, actualizar y
leer datos.

## Requisitos :wrench:

* **sbt version: 1.4.7**
* **Scala version: 2.13.4**
* **postgres SQL**


## Ejecución :hammer:

```
$ sbt run
```

## Endpoints :bulb:
* **createUser:** Crea un usuario dentro de la base de datos por medio de una sentencia SQL
* **updateUser:** Actualiza un usuario dentro de la base de datos por medio de una sentencia SQL
* **deleteUser:** Elimina un usuario dentro de la base de datos por medio de una sentencia SQL
* **getUser:**  Obtiene un usuario dentro de la base de datos por medio de una sentencia SQL
* **listUser:**  Obtiene la lista de los usuarios de la base de datos por medio de una sentencia SQL

## Modo de uso :ledger:

Para utilizar la API se deben realizar peticiones http a la ruta /user de el localhost en el puerto 8000

Ejemplo Básico: 
```
localhost:8000/user/idUser
```
Donde idUser es el usuario al cual le realizaremos operaciones como consulta, o eliminación dependiendo del tipo de petición que realicemos.


###Ejemplos básicos con curl
* **Create:**  curl -d '{"legalId":"123", "firstName":"User", "lastName":"Test", "email":"TestUser@seven4n.com", "phone":"1234567"}' localhost:8000/users
* **Get:** curl localhost:8000/users/123
* **Delete:** curl --verbose -X DELETE localhost:8000/users/123
* **Update:** curl -X post -d '{"legalId":"123", "firstName":"Alejandro", "lastName":"Holguin", "email":"johanholguin@seven4n.com", "phone":"1234567"}' localhost:8000/users
* **List:** curl localhost:8000/users/


## Archivos de pruebas

Al interior encontrará dos archivos de pruebas los cuales son

* **ListSpec:** Pruebas para las funciones de tipo List.
* **NaturalSpec:** Pruebas para las funciones de Natural.
