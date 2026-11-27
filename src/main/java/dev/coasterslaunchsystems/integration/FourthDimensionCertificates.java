package dev.coasterslaunchsystems.integration;

import dev.coasterslaunchsystems.math.Mechanism;
import java.util.List;
import net.minecraft.core.BlockPos;

/** Exact native variants and physical mounting positions; appearance never grants a certificate. */
public final class FourthDimensionCertificates {
  private FourthDimensionCertificates() {}

  public record Certificate(String family, String variant, int longitudinalOffset, boolean cable) {
    public boolean allowed(Mechanism mechanism) { return !mechanism.cable() || cable; }
    public String denial(Mechanism mechanism) {
      if (allowed(mechanism)) return "";
      return switch (variant) {
        case "lateral_swing_v1" -> "Physical cable guide intersects the native suspended cabin sweep";
        case "route_changeover_yaw_v1" -> "Physical cable guide intersects the native flying yaw combined sweep";
        default -> "Native carriage/catch geometry has no compatible rated coupling";
      };
    }
    /** Chassis-local positions relative to the native representative bearing. */
    public BlockPos offset(Mechanism mechanism) {
      return defaultOffset(mechanism).offset(0, 0, mechanism.cable() ? -longitudinalOffset : longitudinalOffset);
    }
  }

  private static final List<Certificate> CERTIFICATES = List.of(
      new Certificate("ARROW", "arrow_four_rail_v1", 0, true),
      new Certificate("ZACSPIN", "zacspin_legacy_v1", 0, true),
      new Certificate("FREE_SPIN", "sns_magnetic_v1", 0, true),
      new Certificate("ZACSPIN_REVISED", "zacspin_revised_v1", 0, true),
      new Certificate("WING", "wing_fixed_v1", 0, true),
      new Certificate("FIXED_INVERTED", "four_abreast_v1", -5, true),
      new Certificate("SPINNING", "conventional_v1", 0, true),
      new Certificate("SPINNING", "launched_inverting_v1", 0, true),
      new Certificate("SUSPENDED", "lateral_swing_v1", -5, false),
      new Certificate("AXIS", "longitudinal_roll_v1", 0, true),
      new Certificate("FLYING", "station_changeover_v1", -5, true),
      new Certificate("FLYING", "route_changeover_yaw_v1", -5, false),
      new Certificate("PIPELINE", "seated_side_rail_v1", -5, true),
      new Certificate("DIVE", "six_abreast_v1", 0, true));

  public static Certificate find(String family, String variant) {
    if ((variant.equals("launched_inverting_v1") || variant.equals("station_changeover_v1")
        || variant.equals("route_changeover_yaw_v1")) && !externalFamilyApi()) return null;
    return CERTIFICATES.stream().filter(c -> c.family.equals(family) && c.variant.equals(variant)).findFirst().orElse(null);
  }
  private static boolean externalFamilyApi() {
    try { return Class.forName("dev.fourthdimension.integration.RideAuthority").getField("EXTERNAL_FAMILY_API").getInt(null) == 1; }
    catch (ReflectiveOperationException unavailable) { return false; }
  }
  public static String denial(String family, String variant, Mechanism mechanism) {
    var certificate = find(family, variant);
    if (certificate != null) return certificate.denial(mechanism);
    return switch (family) {
      case "POWERED_INVERTED" -> "Native whole-circuit Chain governor exclusively owns speed, dwell, reversal and capture";
      case "PAX" -> "PAX winch exclusively owns the native operating cycle";
      case "ROLLER_BALL" -> "Roller Ball requires its native Chain lift, tumble latch, dock and return route";
      default -> "No matching native family/variant certificate";
    };
  }
  public static BlockPos defaultOffset(Mechanism mechanism) {
    return switch (mechanism) {
      case TIRE -> new BlockPos(0, -1, 0);
      case FIN_BRAKE -> new BlockPos(1, -1, 0);
      case SKID_BRAKE -> new BlockPos(0, -2, -1);
      case EDDY_BRAKE -> new BlockPos(-1, -1, 0);
      case LSM -> new BlockPos(0, -1, -1);
      case LIM -> new BlockPos(0, -1, 1);
      case FLYWHEEL, HYDRAULIC, PNEUMATIC -> new BlockPos(0, -2, 0);
    };
  }
}
