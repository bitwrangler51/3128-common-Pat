package common.hardware.motorcontroller;

import java.util.HashSet;
import java.util.LinkedList;

import common.core.NAR_Robot;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;

public abstract class NAR_Motor {
    private static final HashSet<NAR_Motor> leaders = new HashSet<NAR_Motor>();

    static {
        NAR_Robot.addPeriodic(()-> {
            for (final NAR_Motor leader : leaders) {
                final double output = leader.getAppliedOutput();
                for (final NAR_Motor follower : leader.followers) {
                    follower.set(output);
                }
            }
        }, 0.1);
    }

    public enum Control {
        PercentOutput,
        Voltage,
        Velocity,
        Position;
    }

    public enum Neutral {
        BRAKE,
        COAST
    }

    private final LinkedList<NAR_Motor> followers = new LinkedList<NAR_Motor>();
    private double prevValue = 0;
    private Control prevMode = Control.PercentOutput;
    private double prevFeedForward = 0;
    protected double unitConversionFactor = 1;
    protected double timeConversionFactor = 1;

    public void follow(NAR_Motor leader) {
        leader.followers.add(this);
        leaders.add(leader);
    }

    public abstract void enableVoltageCompensation(double volts);

    public void setNeutralMode(Neutral mode) {
        switch(mode) {
            case BRAKE:
                setBrakeMode();
                break;
            case COAST:
                setCoastMode();
                break;
        }
    }

    protected abstract void setBrakeMode();

    protected abstract void setCoastMode();


    public void set(double output) {
        set(output, Control.PercentOutput);
    }

    public void set(double value, Control mode) {
        set(value, mode, 0);
    }

    public void set(double value, Control mode, double feedForward) {
        if (value == prevValue && mode == prevMode && feedForward == prevFeedForward) return;
        prevValue = value;
        prevMode = mode;
        prevFeedForward = feedForward;
        switch(mode) {
            case PercentOutput:
                setPercentOutput(MathUtil.clamp(value, -1, 1));
                break;
            case Voltage:
                setVoltage(MathUtil.clamp(value, -12, 12));
                break;
            case Velocity:
                setVelocity(value, feedForward);
                break;
            case Position:
                setPosition(value, feedForward);
                break;
        }
    }

    public void setUnitConversionFactor(double conversionFactor) {
        this.unitConversionFactor = conversionFactor;
    }

    public void setTimeConversionFactor(double conversionFactor) {
        this.timeConversionFactor = conversionFactor;
    }

    public void resetPosition(double units) {
        resetPosition(units * unitConversionFactor);
    }

    public abstract void setInverted(boolean inverted);

    protected abstract void setPercentOutput(double speed);

    protected abstract void setVoltage(double volts);

    protected abstract void setVelocity(double rpm, double feedForward);

    protected abstract void setPosition(double rotations, double feedForward);

    protected abstract void resetRawPosition(double rotations);

    public abstract double getAppliedOutput();

    public double getPosition() {
        return getRawPosition() / unitConversionFactor;
    }

    public double getVelocity() {
        return getRawVelocity() / unitConversionFactor / timeConversionFactor;
    }

    protected abstract double getRawPosition();

    protected abstract double getRawVelocity();

    public abstract MotorController getMotor();
}
