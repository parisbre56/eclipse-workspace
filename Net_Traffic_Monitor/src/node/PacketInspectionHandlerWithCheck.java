/**
 * 
 */
package node;

import node.sharedMemory.SharedMemory;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;

/** Every time a packet is checked, the exit condition is tested. If the exit condition has been met, it breaks the loop.
 * Otherwise, its functionality is the same as {@link PacketInspectionHandler}.
 * @author Parisbre56
 *
 */
public class PacketInspectionHandlerWithCheck extends PacketInspectionHandler {
	protected Pcap currPcap;
	
	/**
	 * @param pcap The Pcap object the loop is running on, used to break the loop.
	 * @param If The interface the capture is made on
	 * 
	 */
	public PacketInspectionHandlerWithCheck(Pcap pcap, PcapIf If) {
		super(If);
		currPcap=pcap;
	}

	/* (non-Javadoc)
	 * @see org.jnetpcap.packet.PcapPacketHandler#nextPacket(org.jnetpcap.packet.PcapPacket, java.lang.Object)
	 */
	@Override
	public void nextPacket(PcapPacket packet, SharedMemory user) {
		super.nextPacket(packet,user);
		//In addition to handling the packet, also check if the exit condition is met
		//and if so, exit the loop.
		if(Node_Main.exiting.get()==false || Node_Main.sharedMemory.containsPcapIf(currIf)) {
			currPcap.breakloop();
		}
	}
}
