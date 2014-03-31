#Testing part
library(e1071)
args<-commandArgs(TRUE)
model <- load(args[2])
testset<-read.csv(args[1]) #first argument URL
ncol(testset)
svm.pred <- predict(svmModel, testset[,-ncol(testset)])
confusionMatrix<-table(pred = svm.pred, true = testset[,ncol(testset)])
plot(svm.pred)
confusionMatrix
accuracy <- paste("Accuracy: ",classAgreement(confusionMatrix)$diag,sep=" ")
accuracy