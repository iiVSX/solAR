attribute vec4 vPosition;
uniform vec4 u_Color;
uniform mat4 uMVPMatrix;
varying vec4 v_Color;

void main(){
    gl_Position = uMVPMatrix * vPosition;
    v_Color = u_Color;
}