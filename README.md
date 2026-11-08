TheMangos - Sistema de movilidad compartida

Trabajo Práctico Obligatorio de Paradigma Orientada a Objetos.
Propuesta C: sistema para administrar un servicio de bicicletas y monopatines compartidos.

Integrantes: Valentino Cinti (1213639), Ian Holm (1214983), Lautaro Piston (1215780)

Descripción

La aplicación de escritorio permite que los usuarios retiren un vehículo en una estación, lo usen y lo devuelvan en otra. El sistema calcula el importe del viaje y lo descuenta del saldo.
El personal del servicio gestiona las estaciones, los vehículos, el mantenimiento y los traslados.

Funcionalidades por rol

- Usuario (ingresa con documento): Registrarse, gestionar medios de pago, recargar saldo, consultar saldo y movimientos, cambiar su nombre, retirar, cancelar y devolver vehículos, registrar el recorrido y reportar fallas.
- Operador (ingresa con legajo): Reportar fallas, generar órdenes de mantenimiento y registrar traslados entre estaciones.
- Técnico (ingresa con legajo): Cerrar órdenes de mantenimiento y registrar cargas de batería.
- Administrador (ingresa con legajo): Gestionar estaciones, vehículos y personal, y emitir reportes de viajes, uso de estaciones y vehículos con más fallas.

Requisitos

- JDK 17 o superior.
- JavaFX SDK 17 o superior (OpenJFX).
- IntelliJ IDEA (recomendado) u otro IDE para Java.
- Para las pruebas: JUnit 5 (Jupiter) y Mockito.

Estructura del proyecto

- src/controladores: Sistema (arranque) y los controladores de los casos de uso.
- src/usuarios: Usuario, Cuenta, Movimiento, MedioPago, PlanMensual.
- src/viajes: Viaje, PuntoRecorrido, NotaAjuste.
- src/estaciones: Estacion, Traslado.
- src/vehiculos: Vehiculo, BicicletaComun, BicicletaElectrica, MonopatinElectrico, Electrico, ReporteFalla, OrdenMantenimiento.
- src/tarifas: CalculadorTarifa, Tarifa, Promocion y sus implementaciones.
- src/personal: Personal, Operador, Tecnico, Administrador.
- src/reportes: GeneradorReportes.
- src/reloj: Reloj, RelojSistema, RelojFijo.
- src/enums: Tipos y estados.
- src/excepciones: ReglaNegocioException y sus subclases.
- src/vista: Interfaz JavaFX: Main, pantallas (.fxml), controladores de pantalla y estilos.css.
- test: Pruebas unitarias.
- docs: Enunciado y diagramas UML.

Ejecución

Con IntelliJ IDEA:
1. Clonar el repositorio.
2. Abrir la carpeta del proyecto en IntelliJ.
3. Marcar src como Sources Root y test como Test Sources Root.
4. En File -> Project Structure -> Project, elegir un SDK 17 o superior.
5. En File -> Project Structure -> Libraries, agregar la carpeta lib del JavaFX SDK.
7. Ejecutar vista.Main.

Primer uso y datos de prueba

- Al iniciar por primera vez, el sistema carga el personal de prueba:
  - Administrador: legajo 1
  - Operador: legajo 1001
  - Técnico: legajo 2001
- Las tarifas iniciales también se cargan solas:
  - Bicicleta común: desbloqueo $200, minuto $20
  - Bicicleta eléctrica: desbloqueo $300, minuto $45
  - Monopatín eléctrico: desbloqueo $300, minuto $50
- Antes de que los usuarios puedan viajar, el administrador tiene que crear estaciones y vehículos desde su panel.

Persistencia

Los datos se guardan al cerrar la aplicación en el archivo sistema.dat, en la carpeta desde donde se ejecuta, y se cargan al volver a abrirla. Para empezar de cero, alcanza con borrar ese archivo.
