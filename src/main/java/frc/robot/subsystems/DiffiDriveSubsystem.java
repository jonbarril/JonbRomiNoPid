package frc.robot.subsystems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * A differential drive Subsystem with a RomiDriveSubsystem proxy.
 */
public class DiffiDriveSubsystem extends SubsystemBase {
  public DiffiDriveSubsystem(RomiDriveSubsystem proxy) {
    _proxy = proxy;
    _subsystems.add(this);
    _subsystems.addAll(_proxy.getSubsystems());
  }

  /**
   * Gets the current robot pose relative to the play field. The pose is
   * updated automatically as the robot moves relative to the starting pose
   * (i.e.
   * the default pose or that set by setPose()).
   * 
   * @return The pose.
   */
  public Pose2d getPose() {
    return _odometry.getPoseMeters();
  }

  /**
   * Resets the current robot pose relative to the play field. The pose should be
   * that of the physical robot (e.g. at the start of automomous mode).
   * 
   * @param pose
   *        The pose.
   */
  public void resetPose(Pose2d pose) {
    _proxy.resetGyro();
    _proxy.resetEncoders();

    _odometry.resetPosition(_proxy.getRotationZ(), _proxy.getLeftDistance(),
        _proxy.getRightDistance(), pose);
  }

  /**
   * Gets the actual robot relative speed (not neccesarily
   * that set by setDriveSpeeds()).
   * 
   * @return The speeds.
   */
  public ChassisSpeeds getTrueSpeeds() {
    DifferentialDriveWheelSpeeds wheelSpeeds = new DifferentialDriveWheelSpeeds(
        _proxy.getLeftVelocity(), _proxy.getRightVelocity());
    return _kinematics.toChassisSpeeds(wheelSpeeds);
  }

  /**
   * Sets the desired robot relative speed.
   * 
   * @param speeds
   *        The speeds.
   */
  public void setDriveSpeeds(ChassisSpeeds speeds) {
    DifferentialDriveWheelSpeeds wheelSpeeds = _kinematics
        .toWheelSpeeds(speeds);
    setWheelSpeeds(wheelSpeeds);
  }

  /**
   * Gets the subsystems required to support this subsystem, including this one.
   * 
   * @return Temp output group.
   */
  public List<Subsystem> getSubsystems() {
    return Collections.unmodifiableList(_subsystems);
  }

  @Override
  public void periodic() {
    super.periodic();
    _proxy.periodic();

    _odometry.update(_proxy.getRotationZ(), _proxy.getLeftDistance(),
        _proxy.getRightDistance());
  }

  // personal

  private RomiDriveSubsystem _proxy;
  private List<Subsystem> _subsystems = new ArrayList<>();

  private final DifferentialDriveKinematics _kinematics = new DifferentialDriveKinematics(
      RomiDriveSubsystem.WHEEL_TRACKWIDTH_M);
  private final DifferentialDriveOdometry _odometry = new DifferentialDriveOdometry(
      new Rotation2d(), 0.0, 0.0);

  private void setWheelSpeeds(DifferentialDriveWheelSpeeds speeds) {
    // WHEEL_MPS_MAX is based on lots of assumptions
    // (i.e. no load, constant 4.5V, etc.)
    double leftFactor = speeds.leftMetersPerSecond
        / RomiDriveSubsystem.WHEEL_MPS_MAX;
    double rightFactor = speeds.rightMetersPerSecond
        / RomiDriveSubsystem.WHEEL_MPS_MAX;

    // Without PID, need to reduce motor gain and clamp range.
    double gain = 0.066;
    leftFactor *= gain;
    rightFactor *= gain;

    double maxFactor = 1.0;
    leftFactor = Math.max(-maxFactor,
        Math.min(leftFactor, maxFactor));
    rightFactor = Math.max(-maxFactor,
        Math.min(rightFactor, maxFactor));

    _proxy.tankDrive(leftFactor, rightFactor);
  }
}