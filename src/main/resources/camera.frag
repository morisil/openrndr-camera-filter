#version 410

uniform vec2      resolution;
uniform float     time;

uniform sampler2D tex0;
uniform sampler2D tex1;

#define CAMERA_FRAME tex0
#define PREV_FRAME tex1

out vec3 fragColor;


// Spectral Colour Schemes
// By Alan Zucconi
// Website: www.alanzucconi.com
// Twitter: @AlanZucconi

// Example of different spectral colour schemes
// to convert visible wavelengths of light (400-700 nm) to RGB colours.

// The function "spectral_zucconi6" provides the best approximation
// without including any branching.
// Its faster version, "spectral_zucconi", is advised for mobile applications.


// Read "Improving the Rainbow" for more information
// http://www.alanzucconi.com/?p=6703



float saturate (float x)
{
    return min(1.0, max(0.0,x));
}
vec3 saturate (vec3 x)
{
    return min(vec3(1.,1.,1.), max(vec3(0.,0.,0.),x));
}

// --- Spectral Zucconi --------------------------------------------
// By Alan Zucconi
// Based on GPU Gems: https://developer.nvidia.com/sites/all/modules/custom/gpugems/books/GPUGems/gpugems_ch08.html
// But with values optimised to match as close as possible the visible spectrum
// Fits this: https://commons.wikimedia.org/wiki/File:Linear_visible_spectrum.svg
// With weighter MSE (RGB weights: 0.3, 0.59, 0.11)
vec3 bump3y (vec3 x, vec3 yoffset)
{
    vec3 y = vec3(1.,1.,1.) - x * x;
    y = saturate(y-yoffset);
    return y;
}
vec3 spectral_zucconi (float w)
{
    // w: [400, 700]
    // x: [0,   1]
    float x = saturate((w - 400.0)/ 300.0);

    const vec3 cs = vec3(3.54541723, 2.86670055, 2.29421995);
    const vec3 xs = vec3(0.69548916, 0.49416934, 0.28269708);
    const vec3 ys = vec3(0.02320775, 0.15936245, 0.53520021);

    return bump3y (	cs * (x - xs), ys);
}

// --- Spectral Zucconi 6 --------------------------------------------

// Based on GPU Gems
// Optimised by Alan Zucconi
vec3 spectral_zucconi6 (float x)
{

    const vec3 c1 = vec3(3.54585104, 2.93225262, 2.41593945);
    const vec3 x1 = vec3(0.69549072, 0.49228336, 0.27699880);
    const vec3 y1 = vec3(0.02312639, 0.15225084, 0.52607955);

    const vec3 c2 = vec3(3.90307140, 3.21182957, 3.96587128);
    const vec3 x2 = vec3(0.11748627, 0.86755042, 0.66077860);
    const vec3 y2 = vec3(0.84897130, 0.88445281, 0.73949448);

    return
    bump3y(c1 * (x - x1), y1) +
    bump3y(c2 * (x - x2), y2) ;
}


float Perlin3D( vec3 P )
{
    //  https://github.com/BrianSharpe/Wombat/blob/master/Perlin3D.glsl

    // establish our grid cell and unit position
    vec3 Pi = floor(P);
    vec3 Pf = P - Pi;
    vec3 Pf_min1 = Pf - 1.0;

    // clamp the domain
    Pi.xyz = Pi.xyz - floor(Pi.xyz * ( 1.0 / 69.0 )) * 69.0;
    vec3 Pi_inc1 = step( Pi, vec3( 69.0 - 1.5 ) ) * ( Pi + 1.0 );

    // calculate the hash
    vec4 Pt = vec4( Pi.xy, Pi_inc1.xy ) + vec2( 50.0, 161.0 ).xyxy;
    Pt *= Pt;
    Pt = Pt.xzxz * Pt.yyww;
    const vec3 SOMELARGEFLOATS = vec3( 635.298681, 682.357502, 668.926525 );
    const vec3 ZINC = vec3( 48.500388, 65.294118, 63.934599 );
    vec3 lowz_mod = vec3( 1.0 / ( SOMELARGEFLOATS + Pi.zzz * ZINC ) );
    vec3 highz_mod = vec3( 1.0 / ( SOMELARGEFLOATS + Pi_inc1.zzz * ZINC ) );
    vec4 hashx0 = fract( Pt * lowz_mod.xxxx );
    vec4 hashx1 = fract( Pt * highz_mod.xxxx );
    vec4 hashy0 = fract( Pt * lowz_mod.yyyy );
    vec4 hashy1 = fract( Pt * highz_mod.yyyy );
    vec4 hashz0 = fract( Pt * lowz_mod.zzzz );
    vec4 hashz1 = fract( Pt * highz_mod.zzzz );

    // calculate the gradients
    vec4 grad_x0 = hashx0 - 0.49999;
    vec4 grad_y0 = hashy0 - 0.49999;
    vec4 grad_z0 = hashz0 - 0.49999;
    vec4 grad_x1 = hashx1 - 0.49999;
    vec4 grad_y1 = hashy1 - 0.49999;
    vec4 grad_z1 = hashz1 - 0.49999;
    vec4 grad_results_0 = inversesqrt( grad_x0 * grad_x0 + grad_y0 * grad_y0 + grad_z0 * grad_z0 ) * ( vec2( Pf.x, Pf_min1.x ).xyxy * grad_x0 + vec2( Pf.y, Pf_min1.y ).xxyy * grad_y0 + Pf.zzzz * grad_z0 );
    vec4 grad_results_1 = inversesqrt( grad_x1 * grad_x1 + grad_y1 * grad_y1 + grad_z1 * grad_z1 ) * ( vec2( Pf.x, Pf_min1.x ).xyxy * grad_x1 + vec2( Pf.y, Pf_min1.y ).xxyy * grad_y1 + Pf_min1.zzzz * grad_z1 );

    // Classic Perlin Interpolation
    vec3 blend = Pf * Pf * Pf * (Pf * (Pf * 6.0 - 15.0) + 10.0);
    vec4 res0 = mix( grad_results_0, grad_results_1, blend.z );
    vec4 blend2 = vec4( blend.xy, vec2( 1.0 - blend.xy ) );
    float final = dot( res0, blend2.zxzx * blend2.wwyy );
    return ( final * 1.1547005383792515290182975610039 );  // scale things to a strict -1.0->1.0 range  *= 1.0/sqrt(0.75)
}


const float stampFactor = .12;
const float noiseFactor = .001;


void main2() {
    float iTime = time;
    vec2 uv = gl_FragCoord.xy / resolution;
    vec3 cameraColor = texture(CAMERA_FRAME, uv).rgb;
    //vec3 prevColor = texture(PREV_FRAME, uv).rgb;


//    vec2 uv = fragCoord / iResolution.xy;
    vec2 st = (2. * gl_FragCoord.xy - resolution) / resolution.y;
    float dist = length(st);
    float camera = texture(CAMERA_FRAME, uv).r;
    vec2 noiseCoord = st * noiseFactor * (1. + camera * .1);
    vec2 trans = vec2(
    Perlin3D(vec3(noiseCoord, iTime * .5 * (1. + camera * 0.1))),
    Perlin3D(vec3(noiseCoord + 1000., iTime * .5 * (1. + camera * 0.1)))
    ) * Perlin3D(vec3(noiseCoord + 2000., iTime * .1 * (1. + camera * 0.1)))
    * .01 * camera;
    vec3 prevColor = texture(PREV_FRAME, uv - trans - st * .0001).rgb;
    vec3 color = prevColor * .95;
    //float wave = (sin(dist * 10. - iTime) + 1.) * .5;
    //float wave = mod(dist * 2. - iTime * .1, 1.);
    float wave = pow(length(trans * (500. + sin(iTime * .5) * 250.)), 1.);
    color += vec3(.0007, .0001, .002);
    color += spectral_zucconi6(wave) * stampFactor;
    //color += spectral_zucconi6(pow(dist * 1. + length(trans) * 1000., 1.)) * .002;
    fragColor = clamp(color, 0.0, 1.0);

    fragColor = //cameraColor * .01 +
        fragColor * .999;


}


void main() {
    float rainbowFactor = (1 + sin(time * 1.5)) * 20;
    float angleSplitFactor = 1.7;
    float distSplitFactor = .3;
    vec2 uv = gl_FragCoord.xy / resolution;
    vec2 cartUv = (gl_FragCoord.xy - (resolution / 2)) / resolution.y;
    float dist = length(cartUv);
    float distWave = mod(dist - time * .03, distSplitFactor) / distSplitFactor;
    dist = dist - distSplitFactor + mod(dist - time * .03, distSplitFactor);
    float angle = atan(cartUv.x, cartUv.y);
    float angleWave = mod(angle + time * .03, angleSplitFactor) / angleSplitFactor;
    angle = angle - angleSplitFactor + mod(angle + time * .03, angleSplitFactor);
    float x = .5 - dist * sin(angle);
    float y = .5 - dist * cos(angle);
    vec3 color = texture(tex0, 1 - vec2(x, y)).rgb;
    color += (spectral_zucconi6(distWave * rainbowFactor) + spectral_zucconi6((1 - distWave) * rainbowFactor)) * .5;
    color += (spectral_zucconi6(angleWave * rainbowFactor) + spectral_zucconi6((1 - angleWave) * rainbowFactor)) * .5;
    //fragColor = color;
    fragColor = texture(tex0, uv).rgb;
}
