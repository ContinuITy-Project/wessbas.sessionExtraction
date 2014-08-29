pdf("session-stats.pdf")

concurrentSessions.original=read.table("specj_25p_50b_25m/SessionVisitorArrivalAndCompletionRate-sessionsOverTime.csv", header = TRUE, sep = ";")
minTimestamp.original=min(concurrentSessions.original$timestamp)
concurrentSessions.original$expMinute=(concurrentSessions.original$timestamp-minTimestamp.original)/(10^9)/60
plot(concurrentSessions.original$expMinute,concurrentSessions.original$numSessions, type="l", main="Concurrent number of active sessions over time (original)", ylab="#sessions", xlab="Experiment time")
# Note that min or max doesn't make much sense like this. Maybe if we create a step function

ratesAndMaxNumSessions.original=read.table("specj_25p_50b_25m/SessionVisitorArrivalAndCompletionRate-arrivalCompletionRatesAndMaxNumSessions.csv", header = TRUE, sep = ";")
minTimestamp.original=min(ratesAndMaxNumSessions.original$timestamp)
ratesAndMaxNumSessions.original$expMinute=(ratesAndMaxNumSessions.original$timestamp-minTimestamp.original)/(10^9)/60
plot(ratesAndMaxNumSessions.original$expMinute,ratesAndMaxNumSessions.original$arrivalRate, type="l", main="Session arrivals, completions, and concurrency (original)", ylab="#sessions", xlab="Experiment time", col="1")
lines(ratesAndMaxNumSessions.original$expMinute,ratesAndMaxNumSessions.original$completionRate, col="2")
par(new = T)
plot(ratesAndMaxNumSessions.original$expMinute,ratesAndMaxNumSessions.original$maxConcurrentSessions, col="3", type="l", main="",axes = F, xlab = NA, ylab = NA)
axis(side = 4)
mtext(side = 4, line = 3, "y")
legend.text=c("Arrival rates", "Completion rates", "Max active sessions")
legend("bottom", legend = legend.text, col = c(1,2,3), lwd = 1)

sessionLengths.original=read.table("specj_25p_50b_25m/SessionVisitorSessionLengthStatistics-sessionLengths.csv", header = TRUE, sep = ";")
par(mfrow=c(1,4)) 
boxplot(sessionLengths.original, main="Session lengths (original)")
plot(density(sessionLengths.original$length), main="", xlab="")
plot(ecdf(sessionLengths.original$length), main="", xlab="")
hist(sessionLengths.original$length, main="", xlab="")

par(mfrow=c(1,1),mar=c(2,10,1,1))
requestCounts.original=read.table("specj_25p_50b_25m/SessionVisitorRequestTypeCounter-totalRequestsCountsPerAction.csv", header = TRUE, sep = ";")
requestCounts.total.original=sum(requestCounts.original$numRequests)
requestCounts.original$numRequestsRel=requestCounts.original$numRequests/requestCounts.total.original
barplot(requestCounts.original$numRequestsRel, names.arg = requestCounts.original$action, horiz = TRUE,las=1, main="Relative calls to actions (original)")
 
require("xtable")
print.xtable(xtable(requestCounts.original), "requestCounts.tex", type="latex")

dev.off()