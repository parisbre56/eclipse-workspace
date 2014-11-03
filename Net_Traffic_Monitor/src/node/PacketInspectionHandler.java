/**
 * 
 */
package node;

import java.net.InetAddress;
import java.net.UnknownHostException;

import node.sharedMemory.Node_SharedMemory;

import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.Payload;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.network.Ip6;

/** Checks each packet for malicious IPs or payloads if applicable and updates the statistics accordingly
 * @author Parisbre56
 *
 */
public class PacketInspectionHandler implements PcapPacketHandler<Node_SharedMemory> {
	protected PcapIf currIf;
	
	/**
	 * @param If The interface the capture is made on
	 * 
	 */
	public PacketInspectionHandler(PcapIf If) {
		this.currIf=If;
	}

	/* (non-Javadoc)
	 * @see org.jnetpcap.packet.PcapPacketHandler#nextPacket(org.jnetpcap.packet.PcapPacket, java.lang.Object)
	 */
	@Override
	public void nextPacket(PcapPacket packet, Node_SharedMemory user) {
		//If this is an IPv4 packet
		if(packet.hasHeader(Ip4.ID)) {
			Ip4 ip = packet.getHeader(new Ip4());
			try {
				InetAddress addrSource = InetAddress.getByAddress(ip.source());
				InetAddress addrDest = InetAddress.getByAddress(ip.destination());
				user.checkAndAddIPStatIfNecessary(addrSource,addrDest,this.currIf);
			} catch (UnknownHostException e) {
				System.err.println("ERROR: Unable to convert winpcap byte array to internet address v4");
				e.printStackTrace();
			}
		}
		//If this is an IPv6 packet
		else if(packet.hasHeader(Ip6.ID)) {
			Ip6 ip = packet.getHeader(new Ip6());
			try {
				InetAddress addrSource = InetAddress.getByAddress(ip.source());
				InetAddress addrDest = InetAddress.getByAddress(ip.destination());
				user.checkAndAddIPStatIfNecessary(addrSource,addrDest,this.currIf);
			} catch (UnknownHostException e) {
				System.err.println("ERROR: Unable to convert winpcap byte array to internet address v6");
				e.printStackTrace();
			}
		}
		//Else, if this packet does not contain an IP, just check the data, if it has any
		Payload payload = new Payload(); 
		if(packet.hasHeader(payload)) {
			user.checkAndAddPayloadStatIfNecessary(new String(payload.data()),this.currIf);
		}
	}

}
