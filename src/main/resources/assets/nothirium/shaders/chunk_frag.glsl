#version 110

uniform sampler2D u_BlockTex;
uniform sampler2D u_LightTex;

uniform bool u_FogEnabled;
uniform int u_FogMode;
uniform float u_FogStart;
uniform float u_FogEnd;
uniform float u_FogDensity;
uniform vec4 u_FogColor;

varying vec4 v_Color;
varying vec2 v_TexCoord;
varying vec2 v_LightCoord;
varying float v_VertDistance;

float c_smoothstep(float edge0, float edge1, float x) {
    float t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

vec4 linear_fog(vec4 inColor, float vertexDistance) {
    if (vertexDistance <= u_FogStart) {
        return inColor;
    }

    float fogValue = vertexDistance < u_FogEnd ? c_smoothstep(u_FogStart, u_FogEnd, vertexDistance) : 1.0;
    return vec4(mix(inColor.rgb, u_FogColor.rgb, fogValue * u_FogColor.a), inColor.a);
}

vec4 exp_fog(vec4 inColor, float vertexDistance) {
    float fogValue = clamp(1.0 - exp(-u_FogDensity * vertexDistance), 0.0, 1.0);
    return vec4(mix(inColor.rgb, u_FogColor.rgb, fogValue * u_FogColor.a), inColor.a);
}

vec4 exp2_fog(vec4 inColor, float vertexDistance) {
    float fogValue = clamp(1.0 - exp(-u_FogDensity * u_FogDensity * vertexDistance * vertexDistance), 0.0, 1.0);
    return vec4(mix(inColor.rgb, u_FogColor.rgb, fogValue * u_FogColor.a), inColor.a);
}

vec4 apply_fog(vec4 inColor, float vertexDistance) {
    if (!u_FogEnabled) {
        return inColor;
    }

    if (u_FogMode == 0) {
        return linear_fog(inColor, vertexDistance);
    } else if (u_FogMode == 1) {
        return exp_fog(inColor, vertexDistance);
    } else if (u_FogMode == 2) {
        return exp2_fog(inColor, vertexDistance);
    }

    return inColor;
}

void main() {
    vec4 color = v_Color;
    color *= texture2D(u_BlockTex, v_TexCoord);
    color *= texture2D(u_LightTex, v_LightCoord);
    gl_FragColor = apply_fog(color, v_VertDistance);
}