pdf("stats.pdf")

concurrentSessions=read.table("SessionVisitorArrivalAndCompletionRate-sessionsOverTime.csv", header = TRUE, sep = ";")
minTimestamp=min(concurrentSessions$timestamp)
concurrentSessions$expMinute=(concurrentSessions$timestamp-minTimestamp)/(10^9)/60
plot(concurrentSessions$expMinute,concurrentSessions$numSessions, type="l", main="Concurrent number of active sessions over time", ylab="#sessions", xlab="Experiment time")

ratesAndMaxNumSessions=read.table("SessionVisitorArrivalAndCompletionRate-arrivalCompletionRatesAndMaxNumSessions.csv", header = TRUE, sep = ";")
minTimestamp=min(ratesAndMaxNumSessions$timestamp)
ratesAndMaxNumSessions$expMinute=(ratesAndMaxNumSessions$timestamp-minTimestamp)/(10^9)/60
plot(ratesAndMaxNumSessions$expMinute,ratesAndMaxNumSessions$arrivalRate, type="l", main="Session arrivals, completions, and concurrency", ylab="#sessions", xlab="Experiment time", col="1")
lines(ratesAndMaxNumSessions$expMinute,ratesAndMaxNumSessions$completionRate, col="2")
lines(ratesAndMaxNumSessions$expMinute,ratesAndMaxNumSessions$maxConcurrentSessions, col="3")
legend.text=c("Arrival rates", "Completion rates", "Max active sessions")
legend("topright", legend = legend.text, col = c(1,2,3), lwd = 1)

dev.off()