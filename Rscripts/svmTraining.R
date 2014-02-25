#Training part
library(e1071)
args<-commandArgs(TRUE)
Train<-read.csv(args[1]) #first argument URL
numOfcol<-ncol(Train)
x<-Train[,-numOfcol]
y<-Train[,numOfcol]
kerneltype<-args[2]
svmModel<-svm(x,y,kernel=kerneltype,type="C-classification")
model=args[3]
modelName<-paste(model,'.RData',sep="")
save(svmModel, file=modelName)
summary(svmModel)