package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;
import dev.coasterslaunchsystems.runtime.OperatorStatus;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("coasterslaunchsystems_operator")
public final class OperatorStatusGameTests {
  @GameTestGenerator public static List<TestFunction> operators() {
    if (!net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) return List.of();
    return List.of(new TestFunction("ls_operator", "coasterslaunchsystems_operator.shared_status_requests",
        "coasterslaunchsystems_operator:empty", 300, 0, true, OperatorStatusGameTests::run));
  }
  private static void run(GameTestHelper h) {
    var s = new RideControllerGameTests.Scene(h, 10, "arrow_forward", 240);
    var oldRevision = new long[1];
    s.at(40, s::boardFourthDimension);
    s.at(55, () -> { s.install(false); s.controller.setArmed(true); });
    s.at(95, () -> {
      var c=s.controller;var level=s.base.level;var v=s.vehicles.getFirst();var id=(UUID)read(v,"chassis");
      h.assertTrue(c.phase()==RideControllerEntity.State.READY,"Actual LS controller not ready: "+c.status());
      var p=FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"LSOperator"));
      p.setGameMode(GameType.CREATIVE);p.setPos(c.getBlockPos().getCenter());
      var local=OperatorStatus.sample(level,c,id);
      var panel=(CompoundTag)invoke("dev.fourthdimension.tools.OperatorPanel","sample",new Class<?>[]{ServerPlayer.class,UUID.class},p,id);
      h.assertTrue(panel.getCompound("operator").equals(local),"Native and LS status snapshots disagree");
      h.assertTrue(panel.getBoolean("dispatch")&&local.getBoolean("dispatch_ready"),"Registered LS operator not dispatchable: "+panel);
      h.assertTrue(c.status().contains(local.getUUID("owner").toString()),"Local status owner differs");
      long revision=local.getLong("request_revision");
      OperatorStatus.action(level,c,id,13,revision);
      h.assertTrue(c.requestPending()&&(boolean)read(read(v,"authority"),"held"),"Operator request bypassed LS loop/native hold");
      denied(h,()->OperatorStatus.action(level,c,id,13,revision),"replayed operator request");
      h.assertTrue(c.consumeRequest()&&!c.consumeRequest(),"Request not consumed exactly once");
      OperatorStatus.action(level,c,id,10,c.requestRevision());
      h.assertTrue(!c.armed()&&c.phase()==RideControllerEntity.State.INSPECTION&&!c.reservation().isEmpty(),"Stop lost retained ownership");
      OperatorStatus.action(level,c,id,61,c.requestRevision());
      h.assertTrue(c.armed()&&!c.requestPending(),"Resume stored a dispatch request");
      oldRevision[0]=c.requestRevision();
      var saved=c.saveWithoutMetadata(level.registryAccess());
      c.loadWithComponents(saved,level.registryAccess());
      h.assertTrue(!c.armed()&&!c.requestPending()&&c.requestRevision()!=oldRevision[0],"Reload replayed or reused request revision");
      denied(h,()->OperatorStatus.action(level,c,id,13,oldRevision[0]),"pre-reload operator request");
    });
    s.at(120,()->{
      h.assertTrue(s.controller.phase()==RideControllerEntity.State.INSPECTION&&!s.controller.requestPending(),"Reload resumed without operator input");
      System.out.println("LS602_LOCAL_OPERATOR PASS actualArrow=true ownerChargeReasonParity=true transientQueue=true replayDenied=true retainedStop=true resumeNoDispatch=true reloadInvalidatesRequest=true");
      s.cleanup();h.succeed();
    });
  }
  private static void denied(GameTestHelper h,Runnable action,String name){try{action.run();}catch(IllegalArgumentException expected){return;}h.fail("Accepted "+name);}
}
