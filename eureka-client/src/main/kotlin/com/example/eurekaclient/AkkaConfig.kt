package com.example.eurekaclient

import akka.actor.ActorSystem
import akka.actor.Address
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AkkaConfig {

	@Autowired
	lateinit var discoveryClient: DiscoveryClient

	@Bean
	fun system(@Value("\${server.port}") port: Int): ActorSystem {
		println("Used port: $port")
		val akkaConfig = ConfigFactory.parseResources("akka.conf")
		val portConfig = ConfigFactory.parseString("akka.remote.netty.tcp.port=$port")

		val system: ActorSystem = ActorSystem.create("ClusterSystem",
				portConfig.withFallback(akkaConfig))
		val akkaInstances = discoveryClient.getInstances("akka-cluster")

		if (akkaInstances.size == 0) {
			// Is there a seed node already? If no join itself.
			val ownAddress = Address(
					"akka.tcp",
					"ClusterSystem",
					"127.0.0.1",
					port)
			println("==> Self joining cluster: $ownAddress")
			Cluster.get(system).join(ownAddress)
		} else {
			// if yes, just join one of them.
			val addresses = akkaInstances.map {
				Address(
						"akka.tcp",
						"ClusterSystem",
						it.host,
						it.port
				)
			}
			println("==> Joining cluster: $addresses")
			Cluster.get(system).joinSeedNodes(addresses)
		}
		return system
	}
}