package org.inboxview.app;

import org.springframework.boot.SpringApplication;

public class TestInboxviewApplication {

	public static void main(String[] args) {
		SpringApplication.from(InboxviewApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
