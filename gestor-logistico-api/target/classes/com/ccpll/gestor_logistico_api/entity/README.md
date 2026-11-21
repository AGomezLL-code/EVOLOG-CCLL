 Entity se usa principalmente para vincular tu código de Java con una base de datos utilizando el concepto de ORM (Mapeo Objeto-Relacional).Su objetivo principal es hacer que no tengas que escribir consultas SQL manuales para interactuar con tus tablas. 
@Entity: Le indica a Spring Boot que la clase es un modelo de base de datos.
@Table: Define de forma opcional el nombre real de la tabla.
@Id: Marca el atributo que funciona como la clave primaria (Primary Key).
@GeneratedValue: Configura el valor autoincremental para el identificador único.
@Column: Personaliza detalles de la columna como el nombre, si acepta valores nulos o si es única.

