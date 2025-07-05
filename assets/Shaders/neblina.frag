#ifdef GL_ES
precision mediump float;
#endif

// --- UNIFORMES ---
uniform vec2 u_resolution;
uniform float u_radius;
uniform float u_smoothness;
uniform vec4 u_fogColor;
uniform vec2 u_fogCenter; // El centro del círculo en coordenadas de pantalla

void main() {
    // 1. Calculamos la distancia del píxel actual al centro que nos pasó Java
    // En lugar de usar el centro de la pantalla, usamos la posición del personaje
    float distance = length(gl_FragCoord.xy - u_fogCenter);

    // 2. La lógica de suavizado no cambia
    float alpha = smoothstep(u_radius, u_radius + (u_radius * u_smoothness), distance);

    // 3. El color final no cambia
    gl_FragColor = vec4(u_fogColor.rgb, u_fogColor.a * alpha);
}
