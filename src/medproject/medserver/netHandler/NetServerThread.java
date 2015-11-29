package medproject.medserver.netHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import medproject.medlibrary.logging.LogWriter;
import medproject.medserver._serverRunner.MainServer;
import medproject.medserver.dataWriter.DataWriter;
import medproject.medserver.requestHandler.RequestHandler;

public class NetServerThread {
	private final Logger LOG = LogWriter.getLogger("NetServerThread");

	private ServerSocketChannel serverChannel;
	private Selector selector;
	private SelectionKey serverKey;

	private final RequestHandler handlerThread;
	private final DataWriter dataWriter;

	private final NetRead reader;
	private final NetSend sender;

	private List<ChangeRequest> pendingStateChanges = Collections.synchronizedList(new ArrayList<ChangeRequest>());

	public NetServerThread(InetSocketAddress listenAddress) throws Throwable {
		this.dataWriter = new DataWriter(pendingStateChanges);
		this.handlerThread = new RequestHandler(dataWriter);
		this.reader = new NetRead(handlerThread);
		this.sender = new NetSend(dataWriter);

		initializeServer(listenAddress);
	}

	public void start(){
		this.handlerThread.start();
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			processConnections();
		}, 0, 500, TimeUnit.MILLISECONDS);
		LOG.info("Net thread started");
	}

	public void stop(){
		this.handlerThread.stop();
	}

	private void initializeServer(InetSocketAddress listenAddress){
		try {
			this.serverChannel = ServerSocketChannel.open();
			this.serverChannel.configureBlocking(false);
			this.serverKey = serverChannel.register(selector = Selector.open(), SelectionKey.OP_ACCEPT);
			this.serverChannel.bind(listenAddress);
		} catch (IOException e) {
			LOG.severe("Couldn't initialize server");
		}
	}


	void processConnections() {

		synchronized (this.pendingStateChanges) {
			Iterator<ChangeRequest> changes = this.pendingStateChanges.iterator();

			while (changes.hasNext()) {
				ChangeRequest change = (ChangeRequest) changes.next();
				SelectionKey key = change.getSocket().keyFor(this.selector);
				key.interestOps(change.getOps());
			}
			this.pendingStateChanges.clear();
		}

		try {
			selector.selectNow();
		} catch (IOException e) {
			LOG.severe("Couldn't select channels.");
		}

		processSelectedKeys();

		selector.selectedKeys().clear();
	}

	private void processSelectedKeys(){
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

					LOG.info("New client ip=" + acceptedChannel.getRemoteAddress() + ", total clients=" + MainServer.clientMap.size());
				}
				else if (key.isReadable()) {
					ClientSession sesh = MainServer.getClientMap().get(key);

					if (sesh == null)
						continue;

					reader.read(sesh);
				}
				else if (key.isWritable()){
					ClientSession sesh = MainServer.getClientMap().get(key);

					if (sesh == null)
						continue;

					sender.send(sesh);
				}

			} catch (Throwable t) {
				LOG.severe("NetServerThread exception: " + t.getMessage());
			}
		}
	}
}
