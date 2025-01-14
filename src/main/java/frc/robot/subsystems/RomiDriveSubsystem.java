// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.MutDistance;
import edu.wpi.first.units.measure.MutLinearVelocity;
import edu.wpi.first.units.measure.MutVoltage;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.romi.RomiGyro;
import edu.wpi.first.wpilibj.sysid.SysIdRoutineLog;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.jonb.subsystems.DiffDrivable;
import frc.jonb.sysid.SysIdDrivable;

/**
 * A DriveSubsystem for a Romi robot.
 */
public class RomiDriveSubsystem extends SubsystemBase
    implements DiffDrivable, SysIdDrivable {
  public static final double WHEEL_TRACKWIDTH_M = 0.141;
  public static final double WHEEL_ENCODER_CPR = 1440.0;
  public static final double WHEEL_DIAMETER_M = 0.07;
  public static final double WHEEL_CIRCUMFERENCE_M = Math.PI * WHEEL_DIAMETER_M;
  public static final double WHEEL_RPS_MAX = 150.0 / 60.0; // from WPI docs
  public static final double WHEEL_MPS_MAX = WHEEL_RPS_MAX
      * WHEEL_CIRCUMFERENCE_M;

  /** Creates a new Drivetrain. */
  public RomiDriveSubsystem() {
    _subsystems.add(this);

    _rightMotor.setInverted(true);

    _leftEncoder
        .setDistancePerPulse(WHEEL_CIRCUMFERENCE_M / WHEEL_ENCODER_CPR);
    _rightEncoder
        .setDistancePerPulse(WHEEL_CIRCUMFERENCE_M / WHEEL_ENCODER_CPR);
    resetEncoders();
  }

  // DiffDrivable

  @Override
  public double getTrackWidth() {
    return WHEEL_TRACKWIDTH_M;
  }

  @Override
  public double getSpeedMax() {
    return WHEEL_MPS_MAX;
  }

  @Override
  public void arcadeDrive(double forwardFactor, double ccwFactor) {
    _diffDrive.arcadeDrive(forwardFactor, ccwFactor);
  }

  @Override
  public void tankDrive(double leftFactor, double rightFactor) {
    _diffDrive.tankDrive(leftFactor, rightFactor, false);
  }

  @Override
  public void resetEncoders() {
    _leftEncoder.reset();
    _rightEncoder.reset();
  }

  @Override
  public double getLeftDistance() {
    return _leftEncoder.getDistance();
  }

  @Override
  public double getRightDistance() {
    return _rightEncoder.getDistance();
  }

  @Override
  public double getLeftVelocity() {
    return _leftEncoder.getRate();
  }

  @Override
  public double getRightVelocity() {
    return _rightEncoder.getRate();
  }

  @Override
  public void resetGyro() {
    _gyro.reset();
  }

  @Override
  public Rotation2d getRotationZ() {
    return Rotation2d.fromDegrees(-_gyro.getAngleZ());
  }

  @Override
  public Subsystem[] getSubsystems() {
    return _subsystems.toArray(Subsystem[]::new);
  }

  // SysIdDrivable

  @Override
  public void setVoltage(Voltage volts) {
    _leftMotor.setVoltage(volts);
    _rightMotor.setVoltage(volts);
  }

  @Override
  public void logEntry(SysIdRoutineLog log) {
    log.motor("drive-left")
        .voltage(_dummyVoltage.mut_replace(
            _leftMotor.get() * RobotController.getBatteryVoltage(),
            Units.Volts))
        .linearPosition(_dummyDistance.mut_replace(_leftEncoder.getDistance(),
            Units.Meters))
        .linearVelocity(_dummyVelocity.mut_replace(_leftEncoder.getRate(),
            Units.MetersPerSecond));
    log.motor("drive-right")
        .voltage(_dummyVoltage.mut_replace(_rightMotor.get() * RobotController
            .getBatteryVoltage(), Units.Volts))
        .linearPosition(_dummyDistance.mut_replace(_rightEncoder.getDistance(),
            Units.Meters))
        .linearVelocity(_dummyVelocity.mut_replace(_rightEncoder.getRate(),
            Units.MetersPerSecond));
  }

  // personal

  private List<Subsystem> _subsystems = new ArrayList<>();
  private final Spark _leftMotor = new Spark(0);
  private final Spark _rightMotor = new Spark(1);
  private final Encoder _leftEncoder = new Encoder(4, 5);
  private final Encoder _rightEncoder = new Encoder(6, 7);

  private final DifferentialDrive _diffDrive = new DifferentialDrive(
      _leftMotor::set, _rightMotor::set);
  private final RomiGyro _gyro = new RomiGyro();

  private final MutVoltage _dummyVoltage = Units.Volts.mutable(0);
  private final MutDistance _dummyDistance = Units.Meters.mutable(0);
  private final MutLinearVelocity _dummyVelocity = Units.MetersPerSecond
      .mutable(0);

}
