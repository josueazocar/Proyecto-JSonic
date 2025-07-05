// Archivo: shaders/neblina.vert
attribute vec4 a_position;

// La matriz de transformación combinada (proyección * vista)
uniform mat4 u_projTrans;

void main() {
    // Multiplicamos la posición del vértice por la matriz para
    // proyectarla correctamente en el espacio de la pantalla.
    gl_Position = u_projTrans * a_position;
}
