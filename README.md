# UNIDAD 3: Quality Agent

En la unidad 3 se implementa el último paso del proyecto Sistema de Gestión Vehicular para la Policía Nacional, ya que una vez realizada la implementación del mismo basado en Desarrollo Guiado por Especificaciones (SDD), se realiza el análisis de calidad mediante un Quality Agent desarrollado con Claude.

Después del análisis de calidad realizado por la IA podemos responder las siguientes preguntas:

## ¿Qué cambió en tu forma de "dar por terminado" el código cuando el veredicto lo decidió un gate determinista en vez de tu propio criterio?

Lo que cambió radicalmente fue la desaparición de la tolerancia al riesgo negociable. En las pruebas manuales, es común encontrar un defecto menor (un *code smell*, un caso borde rarísimo o una respuesta HTTP que funciona pero no es exactamente la acordada) y pensar que se puede aprobar con observaciones y dejarlo para la próxima iteración.

Con el agente desarrollado esa flexibilidad desaparece. El agente evalúa el código en su totalidad y solo hay 2 opciones: o cumple la especificación o el *pipeline* se bloquea. 

Aprendemos que el código no está listo cuando funciona en un *server* de pruebas, sino cuando la especificación técnica y los criterios de aceptación están codificados con tal claridad que la IA determina si cumple o no.

## ¿Qué pilar te costó más dejar en verde —pruebas, seguridad o criterios—, y por qué?

El pilar más difícil de poner en verde son los criterios de aceptación. Aunque configurar herramientas de cobertura de pruebas unitarias (como JaCoCo en Java) o integrar análisis estático de seguridad pueda parecer complejo, lograr que el pilar de los criterios de aceptación pase a verde es casi siempre el mayor dolor de cabeza.

Los criterios de aceptación, por otro lado, trabajan con la lógica de negocio. Un QA manual tiene el contexto y entiende el giro del negocio. El agente de IA no tiene empatía, solo lee las reglas de calidad y si no cumple, el agente nos va a bloquear. Nos obliga a redactar y desarrollar requerimientos perfectos sin ambigüedades.

## ¿Para qué te serviría un gate de Definition of Done (y el escaneo automático de seguridad vía MCP) en tu equipo real?

En un entorno de producción real, este tipo de herramientas son un elemento fundamental para construir una verdadera fábrica de software autónoma. Sus utilidades principales serían:

* La validación pasa a ser un proceso entre el desarrollador y el estándar de calidad configurado, no entre dos personas con opiniones distintas (eliminando roces con el personal de QA que no aprueba los desarrollos).
* Al detener vulnerabilidades complejas (vía el escaneo del MCP) y fallos de lógica antes de que se haga *merge* a la rama principal, el *Change Failure Rate* debe necesariamente disminuir. A su vez, una vez que el equipo se acostumbra al rigor, el *Lead Time for Changes* mejora porque el código que pasa el *gate* es código que no regresará con *bugs* de producción.
* Un ingeniero de QA manual puede pasar por alto detalles en ciertos escaneos o pruebas si ya está cansado o desmotivado. El agente revisa cientos de líneas de código de manera exhaustiva en segundos, en cualquier momento, garantizando que el *Definition of Done* (DoD) sea un criterio imposible de esquivar en el ciclo de vida del desarrollo.