package gamax92.thistle;

import com.loomcom.symon.Cpu;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import li.cil.oc.api.machine.Context;

public class ThistleVM {
	// The simulated machine
	public ThistleMachine machine;

	// Allocated cycles per tick
	public int cyclesPerTick;

	public ThistleVM(Context context) {
		super();
		try {
			machine = new ThistleMachine(context);
			if (context.node().network() == null) {
				// Loading from NBT
				return;
			}
			machine.getCpu().reset();
			FMLCommonHandler.instance().bus().register(this);
		} catch (Exception e) {
			Thistle.log.warn("Failed to setup Thistle", e);
		}
	}

	void run() throws Exception {
		machine.getComponentSelector().checkDelay();
		Cpu mCPU = machine.getCpu();
		while (mCPU.getCycles() > 0)
			mCPU.step();
		machine.getGioDev().flush();
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		Context context = machine.getContext();
		if (!context.isRunning() && !context.isPaused()) {
			FMLCommonHandler.instance().bus().unregister(this);
			return;
		}
		Cpu mCPU = machine.getCpu();
		if (event.phase == Phase.START && mCPU.getCycles() < cyclesPerTick)
			mCPU.addCycles(cyclesPerTick);
	}
}