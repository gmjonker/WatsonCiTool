import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
//        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %-15.-15logger{15} - %msg%n"
        pattern = "[%logger{0}] %msg%n"
    }
}

logger("gmjonker", DEBUG)

//logger("gmjonker.citool.CiCorpusHelper", TRACE)

root(DEBUG, ["STDOUT"])