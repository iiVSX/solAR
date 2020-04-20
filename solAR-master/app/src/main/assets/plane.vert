attribute vec4 vPosition;
uniform mat4 uMVPMatrix;
attribute vec4 u_Color;
varying vec4 v_Color;

void main(){
    gl_Position = uMVPMatrix * vPosition;
    v_Color = u_Color;
}