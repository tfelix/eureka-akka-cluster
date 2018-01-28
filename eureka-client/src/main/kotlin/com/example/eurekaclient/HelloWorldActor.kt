package com.example.eurekaclient

import akka.actor.AbstractActor

class HelloWorldActor : AbstractActor() {

	override fun createReceive(): Receive {
		return receiveBuilder()
				.match(String::class.java, this::handleText)
				.build()
	}

	private fun handleText(text: String) {
		println("Actor received: $text")
	}
}