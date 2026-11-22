package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Consumer-side persisted-token and explicit operator acknowledgement, with actual native bodies. */
@GameTestHolder("coasterslaunchsystems_authority_recovery")
public final class CurrentAuthorityRecoveryGameTests {
  @GameTestGenerator public static List<TestFunction> recovery() {
    return List.of(new TestFunction("launchsystems_authority_recovery", "coasterslaunchsystems_authority_recovery.saved_token",
        "coasterslaunchsystems_authority_recovery:empty", 400, 0, true, helper -> {
          var s=new RideControllerGameTests.Scene(helper,0,"arrow_forward");
          s.at(40,s::boardFourthDimension);
          s.at(55,()->{s.install(false);s.controller.setArmed(true);});
          s.at(80,()->{
            helper.assertTrue(s.controller.phase()==RideControllerEntity.State.READY,"Initial authority was not ready: "+s.controller.status());
            var v=s.vehicles.getFirst(); var id=(UUID)read(v,"chassis");
            var data=invoke("dev.fourthdimension.runtime.FoundationData","get",new Class<?>[]{ServerLevel.class},s.base.level);
            try {
              var saved=(CompoundTag)data.getClass().getMethod("save",CompoundTag.class,HolderLookup.Provider.class)
                  .invoke(data,new CompoundTag(),s.base.level.registryAccess());
              var loaded=invoke("dev.fourthdimension.runtime.FoundationData","load",new Class<?>[]{CompoundTag.class,HolderLookup.Provider.class},saved,s.base.level.registryAccess());
              var restored=((java.util.Map<?,?>)read(loaded,"vehicles")).get(id);
              v.getClass().getField("authority").set(v,read(restored,"authority"));
            } catch(ReflectiveOperationException failure){throw new IllegalStateException(failure);}
            helper.assertTrue(!MotionProviders.resolve(s.base.level,id).orElseThrow().propulsionAllowed()
                && !MotionProviders.requestDispatch(s.base.level,id),"Saved authority reused a live launch token");
          });
          s.at(84,()->helper.assertTrue(s.controller.phase()==RideControllerEntity.State.FAULT&&!s.controller.reservation().isEmpty(),
              "Restored native ownership did not retain local inspection: "+s.controller.status()));
          s.at(85,()->{s.controller.setArmed(false);s.controller.setArmed(true);});
          s.at(98,()->{
            helper.assertTrue(s.controller.phase()==RideControllerEntity.State.READY,"Explicit re-arm did not resume held authority: "+s.controller.status());
            helper.assertTrue(!MotionProviders.resolve(s.base.level,s.base.parents.getFirst().getUniqueId()).orElseThrow().propulsionAllowed(),"Resume dispatched without a fresh input");
            s.high(true);
          });
          s.at(110,()->{
            var v=s.vehicles.getFirst();var id=(UUID)read(v,"chassis");
            helper.assertTrue(s.controller.phase()==RideControllerEntity.State.RUNNING
                &&!(boolean)read(read(v,"authority"),"held")&&Math.abs(s.base.speed(s.base.parents.getFirst()))>.03,
                "Fresh request after resume did not dispatch and move the native car");
            var token=invoke("dev.fourthdimension.integration.RideAuthority","token",new Class<?>[]{type("dev.fourthdimension.runtime.FoundationData$Vehicle")},v);
            invoke("dev.fourthdimension.integration.RideAuthority","hold",new Class<?>[]{ServerLevel.class,BlockPos.class,UUID.class,type("dev.fourthdimension.integration.RideAuthority$Token")},s.base.level,s.controllerPosition,id,token);
            helper.assertTrue(!(boolean)invoke("dev.fourthdimension.integration.RideAuthority","permits",
                new Class<?>[]{ServerLevel.class,UUID.class,type("dev.fourthdimension.integration.RideAuthority$Token"),String.class},
                s.base.level,id,token,"coasterslaunchsystems:local_controller"),"Foreign hold's new sequence accepted the stale token");
          });
          s.at(145,()->{
            helper.assertTrue(s.controller.phase()==RideControllerEntity.State.FAULT&&!s.controller.reservation().isEmpty()
                &&Math.abs(s.base.speed(s.base.parents.getFirst()))<.03,"Stale-token interruption did not stop with retained ownership");
            System.out.println("LS004_CONSUMER_RECOVERY PASS persisted_token_denied=true explicit_rearm_held=true fresh_request=true stale_sequence_denied=true native_hold=true");
            s.cleanup();helper.succeed();
          });
        }));
  }
}
