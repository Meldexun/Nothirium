#version 110

uniform mat4 u_ModelViewProjectionMatrix;
uniform int u_FogShape;

attribute vec3 a_Pos;
attribute vec4 a_Color;
attribute vec2 a_TexCoord;
attribute vec2 a_LightCoord;
attribute vec3 a_Offset;

varying vec4 v_Color;
varying vec2 v_TexCoord;
varying vec2 v_LightCoord;
varying float v_VertDistance;

float fog_distance(vec3 pos, int shape) {
    if (shape == 1) {
        // cylindrical fog
        return max(length(pos.xz), abs(pos.y));
    } else if (shape == 2) {
        // spherical fog
        return length(pos);
    } else {
        // no fog
        return -1.0;
    }
}

void main() {
    vec3 pos = a_Pos + a_Offset;
    gl_Position = u_ModelViewProjectionMatrix * vec4(pos, 1.0);
    v_Color = a_Color;
    v_TexCoord = a_TexCoord;
    v_LightCoord = vec2((a_LightCoord.x + 8.0) * 0.00390625, (a_LightCoord.y + 8.0) * 0.00390625);
    v_VertDistance = fog_distance(pos, u_FogShape);
}