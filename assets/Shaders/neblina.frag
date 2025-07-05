#ifdef GL_ES
    precision mediump float;
#endif

// "Uniformes": Variables que pasamos desde nuestro código Java.
uniform vec2 u_resolution;  // La resolución de la pantalla (ej. 1280, 720)
uniform float u_radius;     // El radio del círculo visible (en píxeles)
uniform float u_smoothness; // La suavidad del borde del círculo (ej. 0.1)
uniform vec4 u_fogColor;    // El color de la neblina (ej. verde tóxico)
uniform vec2 u_fogCenter;

void main() {
    // 1. Calculamos la coordenada del píxel actual, con el origen (0,0) en el centro.
    vec2 coord = gl_FragCoord.xy - u_resolution / 2.0;

    // 2. Calculamos la distancia de este píxel al centro.
    float distance = length(coord);

    // 3. Calculamos la "suavidad" del borde.
    // smoothstep es una función que crea una transición suave entre 0 y 1.
    // El resultado (alpha) será 0.0 si estamos dentro del círculo, 1.0 si estamos
    // muy afuera, y un valor intermedio si estamos en el borde.
    float alpha = smoothstep(u_radius, u_radius + (u_radius * u_smoothness), distance);

    // 4. Establecemos el color final del píxel.
    // La neblina tendrá el color que le pasamos, pero su transparencia (alpha)
    // dependerá de la transición que calculamos.
    gl_FragColor = vec4(u_fogColor.rgb, u_fogColor.a * alpha);
}
