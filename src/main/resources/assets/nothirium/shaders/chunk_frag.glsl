#version 110

uniform sampler2D u_BlockTex;
uniform sampler2D u_LightTex;

uniform float u_FogStart;
uniform float u_FogEnd;
uniform vec4 u_FogColor;

varying vec4 v_Color;
varying vec2 v_TexCoord;
varying vec2 v_LightCoord;
varying float v_VertDistance;

float smoothstep(float edge0, float edge1, float x) {
    float t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

vec4 linear_fog(vec4 inColor, float vertexDistance, float fogStart, float fogEnd, vec4 fogColor) {
    if (vertexDistance <= fogStart) {
        return inColor;
    }

    float fogValue = vertexDistance < fogEnd ? smoothstep(fogStart, fogEnd, vertexDistance) : 1.0;
    return vec4(mix(inColor.rgb, fogColor.rgb, fogValue * fogColor.a), inColor.a);
}

void main() {
    vec4 color = v_Color;
    color *= texture2D(u_BlockTex, v_TexCoord);
    color *= texture2D(u_LightTex, v_LightCoord);
    gl_FragColor = linear_fog(color, v_VertDistance, u_FogStart, u_FogEnd, u_FogColor);
}