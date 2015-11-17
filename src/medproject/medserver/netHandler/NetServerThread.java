package medproject.medserver.netHandler;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import medproject.medserver._serverRunner.MainServer;
import medproject.medserver.dataWriter.DataWriter;
import medproject.medserver.databaseHandler.DatabaseThread;
import medproject.medserver.requestHandler.RequestHandler;

public class NetServerThread {

	 ServerSocketChannel serverChannel;
     Selector selector;
     SelectionKey serverKey;
     
     
     RequestHandler handlerThread;
     DataWriter dataWriter;
     
     NetRead reader;
     NetSend sender;
     
     private List<ChangeRequest> pendingStateChanges = new LinkedList<ChangeRequest>();


     public NetServerThread(InetSocketAddress listenAddress, DatabaseThread databaseThread) throws Throwable {
             this.serverChannel = ServerSocketChannel.open();
             this.serverChannel.configureBlocking(false);
             this.serverKey = serverChannel.register(selector = Selector.open(), SelectionKey.OP_ACCEPT);
             this.serverChannel.bind(listenAddress);
             
             this.dataWriter = new DataWriter(pendingStateChanges);
             
             this.handlerThread = new RequestHandler(dataWriter, databaseThread);
             this.handlerThread.start();
             
             this.reader = new NetRead(handlerThread);
             this.sender = new NetSend(dataWriter);
             
             Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                     try {
                             processConnections();
                     } catch (Throwable t) {
                             t.printStackTrace();
                     }
             }, 0, 500, TimeUnit.MILLISECONDS);
     }

    
     void processConnections() throws Throwable {
    	 
    	 	synchronized (this.pendingStateChanges) {
				Iterator<ChangeRequest> changes = this.pendingStateChanges.iterator();
				
				while (changes.hasNext()) {
					ChangeRequest change = (ChangeRequest) changes.next();
					SelectionKey key = change.getSocket().keyFor(this.selector);
					key.interestOps(change.getOps());
				}
				
				this.pendingStateChanges.clear();
				
    	 	}
			
    	 	 selector.selectNow();

             for (SelectionKey key : selector.selectedKeys()) {
                     try {
                             if (!key.isValid())
                                     continue;

                             if (key.isAcceptable()) {
                                     SocketChannel acceptedChannel = serverChannel.accept();

                                     if (acceptedChannel == null)
                                             continue;

                                     acceptedChannel.configureBlocking(false);
                                     SelectionKey readKey = acceptedChannel.register(selector, SelectionKey.OP_READ);
                                     MainServer.clientMap.put(readKey, new ClientSession(readKey, acceptedChannel));

                                     System.out.println("New client ip=" + acceptedChannel.getRemoteAddress() + ", total clients=" + MainServer.clientMap.size());
                             }

                             else if (key.isReadable()) {
                                     ClientSession sesh = MainServer.getClientMap().get(key);
                                     System.out.println("Readable");
                                     if (sesh == null)
                                             continue;

                                     reader.read(sesh);
                             }
                             else if (key.isWritable()){
                            	 System.out.println("Writable");
                            	 ClientSession sesh = MainServer.getClientMap().get(key);

                                 if (sesh == null)
                                         continue;

                                 sender.send(sesh);
                             }

                             Thread.sleep(20);
                     } catch (Throwable t) {
                             t.printStackTrace();
                     }
             }

             selector.selectedKeys().clear();
     }
}
	