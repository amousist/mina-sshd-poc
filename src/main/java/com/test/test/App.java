package com.test.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import org.apache.sshd.agent.SshAgent;
import org.apache.sshd.agent.local.LocalAgentFactory;
import org.apache.sshd.agent.unix.AgentClient;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.subsystem.sftp.SftpClient;

public class App {
	public static void main(String[] args) throws InterruptedException {

		try (SshAgent sshAgent = new AgentClient(System.getenv("SSH_AUTH_SOCK"))) {
			// List<Pair<PublicKey, String>> keys = sshAgent.getIdentities();
			// System.out.println(keys.size());

			SshClient sshClient = SshClient.setUpDefaultClient();
			sshClient.setAgentFactory(new LocalAgentFactory(sshAgent));
			sshClient.start();
			try (ClientSession session = sshClient.connect("ueberteufel", "127.0.0.1", 22).verify().getSession()) {
				session.auth().verify();
				try (SftpClient sftp = session.createSftpClient()) {
					try {
						sftp.remove("/tmp/test-dir/test-file.txt");
						sftp.rmdir("/tmp/test-dir");
					} catch (Exception e) {
						//file or dir does not exist
					}

					sftp.mkdir("/tmp/test-dir");
					String remotePath = "/tmp/test-dir/test-file.txt";
					String data = "This is a test\n";
					try (SftpClient.CloseableHandle handle = sftp.open(remotePath,
							EnumSet.of(SftpClient.OpenMode.Write, SftpClient.OpenMode.Create))) {
						sftp.write(handle, 0, data.getBytes(StandardCharsets.UTF_8), 0, data.length());
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
