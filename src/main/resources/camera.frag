#version 330

uniform vec2      resolution;
uniform float     time;

uniform sampler2D tex0;

out vec3 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution;
    fragColor = texture(tex0, uv).rgb;
}
