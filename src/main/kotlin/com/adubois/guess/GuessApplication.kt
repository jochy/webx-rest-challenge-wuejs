package com.adubois.guess

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class Token(val id: Int, val token: UUID)
class TryRequest(val token: UUID, val guess: Int)
class TryAnswer(val code: Int)

@SpringBootApplication
class GuessApplication

fun main(args: Array<String>) {
    runApplication<GuessApplication>(*args)
}


@RestController
class RestController {

    val cache: Cache<UUID, Int> = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(10_000)
            .build()

    val currentTokenIndex = AtomicInteger(0)

    @GetMapping("/token")
    fun token(): Token {
        val token = Token(id = currentTokenIndex.getAndIncrement(), token = UUID.randomUUID())

        cache.put(token.token, (Math.random() * 1000).toInt())

        return token
    }

    @PostMapping("/try")
    fun tryGuess(@RequestBody request: TryRequest): ResponseEntity<TryAnswer> {
        if (cache.getIfPresent(request.token) == null) {
            return ResponseEntity.badRequest().build();
        }

        val toGuess = cache.getIfPresent(request.token)!!
        if (toGuess == request.guess) {
            return ResponseEntity.ok(TryAnswer(0))
        } else if (toGuess < request.guess) {
            return ResponseEntity.ok(TryAnswer(-1))
        } else {
            return ResponseEntity.ok(TryAnswer(1))
        }
    }

}