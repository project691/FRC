/*
 * TEAM     : 691 Hart Burn
 * AUTHOR(S): Akira "Ninja" H., Casey G., Gerard "B-Bo" B.,
 *            Bryan "Brandon" S. (Don't call him Brandon), Robert "Sir Sayer" G.,
 *            Shoma H.
 * SEASON   : 2011
 * CONTACT  : mheid2011@gmail.com
 */

/*
 * LAST EDITED:
 * 7/30/2011 by Gerard
 */

package Team691.DriveSystems.Meccanum;
import Team691.DriveSystems.Base.*;
import Team691.Util.Time;
import Team691.Util.Mathf;

import edu.wpi.first.wpilibj.Encoder;

/**
 * Controls the wheel movement for a standard meccanum drive setup.
 * @author Akira
 */
public class MeccanumDrive implements Drive
{
    protected MeccanumWheel[] wheels = new MeccanumWheel[4];
    protected double[] magnitudes = new double[wheels.length];
    protected double[] lastMagnitudes = new double[wheels.length];

    protected boolean shouldSmoothVelocityOverTime = false;
    protected double smoothingFactor = 8.0;

    /**
     * Constructor for the MeccanumDrive object.
     * @param velMtrsIn A length 4 array of Velocity motors where [0] is the FR
     * motor, [1] is the FL motor, [2] is the BL motor, and [3] is the BR motor.
     */
    public MeccanumDrive(PIDControlledVelocityMotor[] velMtrsIn)
    {
        for(int count = 0; count < wheels.length; count++)
        {    wheels[count] = new MeccanumWheel(velMtrsIn[count], count + 1);    }
    }

    /**
     * Constructor for the MeccanumDrive object. This method is only used for
     * backwards compatability.
     * @param channelNum The array of channel numbers for the speed controllers
     * ([0] is Front Right Wheel, [1] is Front Left Wheel, [2] is Back Left
     * Wheel, [3] is Back Right Wheel).
     * @param FREnc The Front Right Wheel encoder object.
     * @param FLEnc The Front Left Wheel encoder object.
     * @param BREnc The Back Right Wheel encoder object.
     * @param BLEnc The Back Left Wheel encoder object.
     */
    public MeccanumDrive(int channelNum[],Encoder FREnc,Encoder FLEnc,Encoder BREnc,
            Encoder BLEnc)
    {

       wheels[0] = new MeccanumWheel(channelNum[0],FREnc,1);
       wheels[1] = new MeccanumWheel(channelNum[1],FLEnc,2);
       wheels[2] = new MeccanumWheel(channelNum[2],BLEnc,3);
       wheels[3] = new MeccanumWheel(channelNum[3],BREnc,4);
    }

    /**
     * Updates all four wheel speeds.
     * @param LY The value from the left joystick's Y-Axis.
     * @param LX The value from the left joystick's X-Axis.
     * @param RX The value from the right joysticks' X-Axis.
     */
    public void update(double LY,double LX,double RX)
    {
        // uses fewer processes if  all values are 0
        if(LY == 0 && LX == 0 && RX == 0)
        {
            for(int count = 0; count < wheels.length; count++)
            {
                wheels[count].setMotor(0);
                lastMagnitudes[count] = 0;
            }
        }
        else//normal operation
        {
            //send the Joystick data to the wheels
            double max = -1;
            for(int count = 0; count < wheels.length; count++)
            {
                //update calculates the magnitude that this wheel should move at
                //  , but does not actually set the motor - gives us a chance to
                //      syncronize the wheel's speeds
                magnitudes[count] = wheels[count].update(LY, LX, RX);
                double temp = Mathf.abs(magnitudes[count]);
                if( temp > max)
                    max = temp;
            }

            //now we actually tell the motors how to move
            for(int count = 0; count < wheels.length; count++)
            {
                //We can't set the motor's power to more than 100%, scale everything
                //      down equaly
                if(max > 1)
                {   magnitudes[count] /= max;   }

                //Allows for less violent changes in motor power
                if(shouldSmoothVelocityOverTime)
                {
                    magnitudes[count] = Mathf.lerp
                            (   lastMagnitudes[count], magnitudes[count],
                                Time.deltaTime() * smoothingFactor   );
                }

                //now we move the motors
                wheels[count].setMotor(magnitudes[count]);
                lastMagnitudes[count] = magnitudes[count];
            }
        }
    }

    /**
     * Stops the drive system by turning off all the motors.
     */
    public void stop()
    {   update(0,0,0);   }

    /**
     * Provides a nicely formatted way to read off all the important numbers in
     * the drive system.
     * @return A human readable string containing the current motor setting and
     * encoder velocity readings.
     */
    public String toString()
    {
        return  ( "\n\nFRMag: " + wheels[0].getMagnitude()  + "\t\t\tFREncVel: " + wheels[0].toString()
                  + "\nFLMag: " + wheels[1].getMagnitude()  + "\t\t\tFLEncVel: " + wheels[1].toString()
                  + "\nBLMag: " + wheels[2].getMagnitude()  + "\t\t\tBLEncVel: " + wheels[2].toString()
                  + "\nBRMag: " + wheels[3].getMagnitude()  + "\t\t\tBREncVel: " + wheels[3].toString()
                );
    }
}