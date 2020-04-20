package edu.skku.curvRoof.solAR.Utils;

import android.util.Log;

import edu.skku.curvRoof.solAR.Model.User;
import edu.skku.curvRoof.solAR.R;

public class elefeeCal {
    private double radiation = 3.57f;
    private double panelinfo = 7.5f;    //1.64 x 0.99 x 15.4 x 30(1month)
    public double userfee;  //월평균 전기세
    public double money;    //예상 전기세
    public double generate;    //예상 발전량

    public double calUserfee(User user,int n, int m, double direction, double angle){

        userfee = user.getElec_fee();
        int panelnum = n*m;

        double monthlyuse = 0;  //userfee를 통해 알아낸 월 평균 전기 사용량

        double resulta ;
        double resultb ;
        double resultab;
        double result;  // 월평균 사용량 - 예상 발전량

        Log.d("panelnum", String.valueOf(panelnum));
        Log.d("userfee", String.valueOf(userfee));
//        longitude = trial.getLongitude();
//        latitude = trial.getLatitude();
        /**
         * 1.DB에서 사용자의 전기세 받아오기.
         * 2.위치정보 받아서 DB에서 일사량 가져오기.
         * 3.면적통해서 개수 받아오기.
         * **/

        double temp;
        //유저의 전기세를 바탕으로 사용전력량 계산
        if (userfee <= 17960) {
            //printf("태양광 발전을 필요로 하지 않습니다.");
        }
        else if (userfee <= 65760) {
            temp = userfee / 1.137;
            monthlyuse = ((temp - 20260) / 187.9) + 200;
        }
        else {
            temp = userfee / 1.137;
            monthlyuse = ((temp - 57840) / 280.6) + 400;
        }
        generate = panelinfo * panelnum * radiation; //발전량 계산
        Log.d("monthlyuse", String.valueOf(monthlyuse));
        Log.d("generate", String.valueOf(generate));
        //expectGen.setText(generate);

        //방향
        resulta = 100 - 0.1072 * Math.abs(direction-180) - 0.0112 * Math.abs(direction-180) * Math.abs(direction-180);
        //각도
        resultb = 89.796 + 0.6227 * angle - 0.0095 * angle * angle;
        //전체효율
        resultab = resulta*resultb/100;
        generate = generate*resultab/100;
        result = monthlyuse - generate;
        Log.d("resultab", String.valueOf(resultab));
        Log.d("result", String.valueOf(result));

        //예상 전기료 계산
        if (result <= 200) {
            temp = 910 + 93.3 * result;
            if (temp < 5000) money = 1130;
            else {
                money = (temp - 4000) * 1.137;
            }
        }
        else if (result <= 400) {
            temp = 20260 + ((result - 200) * 187.9);
            money = temp * 1.137;
        }
        else {
            temp = 57840 + ((result - 400) * 280.6);
            money = temp * 1.137;
        }

        return money;
    }
}
