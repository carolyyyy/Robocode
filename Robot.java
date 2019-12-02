package  yangxiaotong;
import robocode.*;
import java.awt.geom.Point2D;
import java.awt.Color;
public class Robot extends AdvancedRobot
{
    Enemy target;
    final double PI = Math.PI;
    int direction = 1;
    double firePower;
    public void run()
    {
        doInit();
        target = new Enemy();
        target.distance = 100000;
        setColors(Color.red,Color.red,Color.red);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        turnRadarRightRadians(2*PI);
        while(true)
        {
            doMovement();
            doFirePower();
            doScanner();

            doGun();
            out.println(target.distance);
            fire(firePower);
            execute();
        }
    }
    public void doInit()
    {
        double myX = getX();
        double myY = getY();
        double nextX, nextY;

        nextX = 400;
        nextY = 300;

        double turnAngle = Enemy.getAngle( myX, myY,nextX, nextY );

        turnAngle = NormaliseBearing(turnAngle - getHeadingRadians());

        double moveDistance = Point2D.distance( myX,myY, nextX, nextY );

        double moveDirection = 1;

        if ( Math.abs( turnAngle ) > Math.PI/2 )
        {

            turnAngle= NormaliseBearing(turnAngle + Math.PI );
            moveDirection = -1;

            setTurnRightRadians( turnAngle );
            setAhead( moveDirection * moveDistance );
        }
    }
    void doFirePower()
    {
        firePower = 400/target.distance;
    }
    void doMovement()
    {
        if (getTime()%20 == 0)
        {
            direction *= -1;
            setAhead(direction*350);
        }
        setTurnRightRadians(target.bearing + (PI/2));
    }
    void doScanner()
    {
        double radarOffset;
        if (getTime() - target.ctime >4)
        {
            radarOffset = 400;      //rotate the radar to find a target
        }
        else
        {

            radarOffset = getRadarHeadingRadians() - absbearing(getX(),getY(),target.x,target.y);

            if (radarOffset < 0)
                radarOffset -= PI/8;  //(0.375)
            else
                radarOffset += PI/8;
        }
        setTurnRadarLeftRadians(NormaliseBearing(radarOffset));
    }
    void doGun()
    {
        long time = getTime() + (int)(target.distance/(20-(3*firePower)));

        double gunOffset = getGunHeadingRadians() - absbearing(getX(),getY(),target.guessX(time),target.guessY(time));
        setTurnGunLeftRadians(NormaliseBearing(gunOffset));
    }
    double NormaliseBearing(double ang)
    {
        if (ang > PI)
            ang -= 2*PI;
        if (ang < -PI)
            ang += 2*PI;
        return ang;
    }
    double NormaliseHeading(double ang)
    {
        if (ang > 2*PI)
            ang -= 2*PI;
        if (ang < 0)
            ang += 2*PI;
        return ang;
    }
    public double getrange( double x1,double y1, double x2,double y2 )
    {
        double xo = x2-x1;
        double yo = y2-y1;
        double h = Math.sqrt( xo*xo + yo*yo );
        return h;
    }
    public double absbearing( double x1,double y1, double x2,double y2 )
    {
        double xo = x2-x1;
        double yo = y2-y1;
        double h = getrange( x1,y1, x2,y2 );

        if( xo > 0 && yo > 0 )
        {
            return Math.asin( xo / h );
        }
        if( xo > 0 && yo < 0 )
        {
            return Math.PI - Math.asin( xo / h );
        }
        if( xo < 0 && yo < 0 )
        {
            return Math.PI + Math.asin( -xo / h );
        }
        if( xo < 0 && yo > 0 )
        {
            return 2.0*Math.PI - Math.asin( -xo / h );
        }
        return 0;
    }
    public void onScannedRobot(ScannedRobotEvent e)
    {
        if ((e.getDistance() < target.distance)||(target.name == e.getName()))
        {

            double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*PI);
            //this section sets all the information about our target
            target.name = e.getName();

            target.x = getX()+Math.sin(absbearing_rad)*e.getDistance(); //works out the x coordinate of where the target is
            target.y = getY()+Math.cos(absbearing_rad)*e.getDistance(); //works out the y coordinate of where the target is
            target.bearing = e.getBearingRadians();
            target.head = e.getHeadingRadians();
            target.ctime = getTime();               //game time at which this scan was produced
            target.speed = e.getVelocity();
            target.distance = e.getDistance();
        }
    }
    public void onRobotDeath(RobotDeathEvent e)
    {
        if (e.getName() == target.name)
            target.distance = 100000;   //this will effectively make it search for a new target
    }
}

class Enemy
{
    String name;
    public double bearing;
    public double head;
    public long ctime; //game time that the scan was produced
    public double speed;
    public double x,y;
    public double distance;
    public double guessX(long when)
    {

        long diff = when - ctime;
        return x+Math.sin(head)*speed*diff;
    }
    public double guessY(long when)
    {
        long diff = when - ctime;
        return y+Math.cos(head)*speed*diff;
    }
    public static double getAngle(double x1, double y1, double x2, double y2)
    {
        return Math.atan2( x2 - x1, y2 - y1 );
    }
}




