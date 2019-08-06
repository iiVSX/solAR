package edu.skku.curvRoof.solAR.Activity;

import com.curvsurf.fsweb.*;
import java.nio.*;
import java.io.*;
import java.util.*;

class MyTest
{
    public static final String REQUEST_URL = "http://192.168.123.50:8080/FindSurfaceWeb/ReqFS.do";
    public static final String POINT_PATH = "/Users/curvsurf/develop/CurvSurf/SamplePointData/sample.xyz";

    public static FloatBuffer ReadPointData(String fileName) throws Exception {
        BufferedReader reader = new BufferedReader( new FileReader( fileName ) );
        LinkedList<String> lines = new LinkedList<String>();

        for(;;) {
            String line = reader.readLine();
            if(line == null) break;
            if( line.split("\\s").length > 2 ) {
                lines.add(line);
            }
        }

        int pointCount = lines.size();
        ByteBuffer buffer = ByteBuffer.allocate( (Float.SIZE / 8) * 3 * pointCount );
        buffer.order(ByteOrder.nativeOrder());

        for(String line : lines) {
            String[] columns = line.split("\\s");

            buffer.putFloat( Float.parseFloat(columns[0]) );
            buffer.putFloat( Float.parseFloat(columns[1]) );
            buffer.putFloat( Float.parseFloat(columns[2]) );
        }
        // Reset Position
        buffer.position(0);

        return buffer.asFloatBuffer();
    }

    public static void main(String args[]) throws Exception
    {
		// Ready Point Cloud
        FloatBuffer points = ReadPointData(POINT_PATH);

		// Ready Request Form
        ReqForm rf = new ReqForm();
        
        rf.pointCount  = points.capacity() / 3;
        rf.pointStride = 0;

        rf.seedIndex = 7827;
        rf.accuracy  = 0.003f;
        rf.meanDist  = 0.05f;
        rf.touchR    = 0.05f;

        rf.findType  = 2; // 1 - Plane, 2 - Sphere, 3 - Cylinder, 4 - Cone, 5 - Torus
        rf.radExp    = 5;
        rf.latExt    = 10;
        rf.option    = 0;

        FindSurfaceRequester fsr = new FindSurfaceRequester(REQUEST_URL, true);

		// Request Find Surface
        RespForm resp = fsr.request(rf, points);
        if(resp != null) {
            System.out.println( String.format("Result: %d\nRMS: %g", resp.fsResult, resp.rms) );
            if( resp.fsResult == 2 ) { // fsResult: 0 - Not Found, 1 - Plane, 2 - Sphere, 3 - Cylinder, 4 - Cone, 5 - Torus
                RespForm.SphereParam param = resp.getParamAsSphere();
                System.out.println( "Radius: " + param.r );
                System.out.println( String.format("Center: %g, %g, %g", param.c[0], param.c[1], param.c[2]) );
            }
        }
    }
}