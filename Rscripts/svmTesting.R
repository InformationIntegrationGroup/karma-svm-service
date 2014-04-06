#Testing part
library(e1071)

args<-commandArgs(TRUE)
args
model <- load(args[2])
predictedFileName <- args[3]
confusionMatrixFile <- args[4]

testset<-read.csv(args[1],head=T,sep=',') #first argument URL
numOfcol<-ncol(testset)

x<-testset[,-numOfcol]
y<-testset[,numOfcol]

svm.pred <- predict(svmModel, x)

testset['predicted'] <- fitted(svmModel)
#testset[1:8,]

confusionMatrix <- table(fitted(svmModel), y)
pred <- predict(svmModel, x, decision.values = TRUE)

#summary(svmModel)
#attr(pred, "decision.values")[1:8,]

write.table(testset, predictedFileName, sep=',', row.names=FALSE, quote = FALSE)

#plot(svm.pred)

write.table(confusionMatrix,file=confusionMatrixFile,sep=',',col.names=NA, quote = FALSE)

accuracy <- paste("Accuracy: ",classAgreement(confusionMatrix)$diag,sep=" ")
accuracy
