#Testing part
library(e1071)
args<-commandArgs(TRUE)
model <- load(args[2])
#model <- load("/Users/shaarif/Desktop/modTransport/transportModel.RData")
testset<-read.csv(args[1]) #first argument URL
#testset <- read.csv("/Users/shaarif/Desktop/modTransport/test.csv")
ncol(testset)
svm.pred <- predict(svmModel, testset[,-ncol(testset)])
confusionMatrix<-table(pred = svm.pred, true = testset[,ncol(testset)])
plot(svm.pred)
confusionMatrix
accuracy <- paste("Accuracy: ",classAgreement(confusionMatrix)$diag,sep=" ")
accuracy