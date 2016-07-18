package com.c35.mtd.pushmail.util.extractsign;




/**
 * C5算法得到的决策树分类器
 * 
 * @author yangxf1
 *
 */
public class JudgeByC5 extends Judge {

	@Override
	public boolean isScoreAboveThreshold(int score) {
		// TODO Auto-generated method stub
		
		
		return score > 50;
	}

	public boolean isSingleLineSign(LineFeature.Feature[] features, int curLine){
		return classify(features, curLine) > 50;
	}
	
	public int classify(LineFeature.Feature[] features, int curLine) {

		
		LineFeature.Feature feature_current = features[curLine];
		if (feature_current == null)
			feature_current = new LineFeature.Feature();

		LineFeature.Feature feature_prev;
	
		if (curLine >= 2){
			feature_prev = featureOr(features[curLine-1],features[curLine-2]);
		}else if (curLine == 1)
			feature_prev = featureOr(features[curLine-1],null);
		else 
			feature_prev =  featureOr(null,null);

		LineFeature.Feature feature_after;
		
		if (curLine < features.length - 2){
			feature_after = featureOr(features[curLine+1],features[curLine+2]);
		}else if (curLine == features.length - 2)
			feature_after = featureOr(features[curLine+1],null);
		else 
			feature_after = featureOr(null,null);
		
		int datePattern = feature_current.datePattern?1:0; 
		int emailPattern = feature_current.emailPattern?1:0;
		int likeAddress = feature_current.likeAddress?1:0;
		int likeAName = feature_current.likeAName?1:0;
		int lineMark = feature_current.lineMark?1:0;
		int lotsSpecialSymbols = feature_current.lotsSpecialSymbols?1:0;
		int phoneNumberPattern = feature_current.phoneNumberPattern?1:0;
		int senderEmail = feature_current.senderEmail?1:0;
		int senderName = feature_current.senderName?1:0;
		int startWithReplyMark = feature_current.startWithReplyMark?1:0;
		int typicalReplyWords = feature_current.typicalReplyWords?1:0;
		int typicalSignWords = feature_current.typicalSignWords?1:0;
		int urlPattern = feature_current.urlPattern?1:0;
		

		int datePattern_prev = feature_prev.datePattern?1:0; 
		int emailPattern_prev = feature_prev.emailPattern?1:0;
		int likeAddress_prev = feature_prev.likeAddress?1:0;
		int likeAName_prev = feature_prev.likeAName?1:0;
		int lineMark_prev = feature_prev.lineMark?1:0;
		int lotsSpecialSymbols_prev = feature_prev.lotsSpecialSymbols?1:0;
		int phoneNumberPattern_prev = feature_prev.phoneNumberPattern?1:0;
		int senderEmail_prev = feature_prev.senderEmail?1:0;
		int senderName_prev = feature_prev.senderName?1:0;
		int startWithReplyMark_prev = feature_prev.startWithReplyMark?1:0;
		int typicalReplyWords_prev = feature_prev.typicalReplyWords?1:0;
		int typicalSignWords_prev = feature_prev.typicalSignWords?1:0;
		int urlPattern_prev = feature_prev.urlPattern?1:0;

		
		int datePattern_after = feature_after.datePattern?1:0; 
		int emailPattern_after = feature_after.emailPattern?1:0;
		int likeAddress_after = feature_after.likeAddress?1:0;
		int likeAName_after = feature_after.likeAName?1:0;
		int lineMark_after = feature_after.lineMark?1:0;
		int lotsSpecialSymbols_after = feature_after.lotsSpecialSymbols?1:0;
		int phoneNumberPattern_after = feature_after.phoneNumberPattern?1:0;
		int senderEmail_after = feature_after.senderEmail?1:0;
		int senderName_after = feature_after.senderName?1:0;
		int startWithReplyMark_after = feature_after.startWithReplyMark?1:0;
		int typicalReplyWords_after = feature_after.typicalReplyWords?1:0;
		int typicalSignWords_after = feature_after.typicalSignWords?1:0;
		int urlPattern_after = feature_after.urlPattern?1:0;
		
		int first1Sent =  0;
		int first2Sent =  0;
		int last2Sent =  0;
		int last1Sent =  0;
		
		if (curLine == 0)
			first1Sent = 1;
		else if (curLine == 1)
			first1Sent = 1;
		else if (curLine == features.length - 2)
			last2Sent = 1;
		else if (curLine == features.length - 1)
			last1Sent = 1;
				
		int length = feature_current.length;
		
		  if ( typicalSignWords == 1 ){
		      if ( length > 25 ) return -99;
		      if ( length <= 25 ){
		          if ( first2Sent == 1 ) return -89;
		          if ( first2Sent == 0 ){
		              if ( urlPattern_after == 1 ) return 99;
		              if ( urlPattern_after == 0 ){
		                  if ( emailPattern_after == 1 ){
		                      if ( typicalReplyWords_after == 1 ) return 99;
		                      if ( typicalReplyWords_after == 0 ){
		                          if ( startWithReplyMark_after == 1 ) return -72;
		                          if ( startWithReplyMark_after == 0 ){
		                              if ( senderEmail_after == 1 ) return 99;
		                              if ( senderEmail_after == 0 ){
		                                  if ( senderName_after == 0 ) return 85;
		                                  if ( senderName_after == 1 ) return -80;
		                              }
		                          }
		                      }
		                  }
		                  if ( emailPattern_after == 0 ){
		                      if ( senderName_after == 1 ) return -92;
		                      if ( senderName_after == 0 ){
		                          if ( startWithReplyMark_after == 1 ) return -85;
		                          if ( startWithReplyMark_after == 0 ){
		                              if ( typicalSignWords_prev == 0 ){
		                                  if ( typicalSignWords_after == 1 ) return 93;
		                                  if ( typicalSignWords_after == 0 ){
		                                      if ( urlPattern_prev == 1 ) return 95;
		                                      if ( urlPattern_prev == 0 ){
		                                          if ( emailPattern == 1 ) return 92;
		                                          if ( emailPattern == 0 ) 
		                                           {
		                                            if ( phoneNumberPattern_after == 0 ) return -97;
		                                            if ( phoneNumberPattern_after == 1 ) return 75;
		                                           }
		                                      }
		                                  }
		                              }
		                              if ( typicalSignWords_prev == 1 ){
		                                  if ( datePattern_prev == 1 ) return 99;
		                                  if ( datePattern_prev == 0 ){
		                                      if ( emailPattern_prev == 1 ) return 99;
		                                      if ( emailPattern_prev == 0 ){
		                                          if ( emailPattern == 1 ) return 97;
		                                          if ( emailPattern == 0 ){
		                                              if ( length <= 2 ) return -84;
		                                              if ( length > 2 ){
		                                                  if ( likeAddress == 1 ) return 94;
		                                                  if ( likeAddress == 0 ) 
		                                                   {
		                                                    if ( datePattern_after == 1 ) return 93;
		                                                    if ( datePattern_after == 0 ){
		                                                        if ( startWithReplyMark == 0 ) return -90;
		                                                        if ( startWithReplyMark == 1 ) return 89;
		                                                    }
		                                                   }
		                                              }
		                                          }
		                                      }
		                                  }
		                              }
		                          }
		                      }
		                  }
		              }
		          }
		      }
		  }
		  if ( typicalSignWords == 0 ){
		      if ( urlPattern == 1 ) return 98;
		      if ( urlPattern == 0 ){
		          if ( phoneNumberPattern_after == 1 ){
		              if ( first1Sent == 1 ) return -92;
		              if ( first1Sent == 0 ){
		                  if ( typicalSignWords_after == 1 ) return 97;
		                  if ( typicalSignWords_after == 0 ){
		                      if ( senderEmail_after == 1 ) return 83;
		                      if ( senderEmail_after == 0 ){
		                          if ( startWithReplyMark_prev == 0 ) return -76;
		                          if ( startWithReplyMark_prev == 1 ) return 80;
		                      }
		                  }
		              }
		          }
		          if ( phoneNumberPattern_after == 0 ){
		              if ( likeAddress_after == 1 ) return 97;
		              if ( likeAddress_after == 0 ){
		                  if ( lotsSpecialSymbols_prev == 1 ){
		                      if ( datePattern == 1 ) return -75;
		                      if ( datePattern == 0 ){
		                          if ( length > 3 ) return 97;
		                          if ( length <= 3 ){
		                              if ( lotsSpecialSymbols == 0 ) return -83;
		                              if ( lotsSpecialSymbols == 1 ) return 80;
		                          }
		                      }
		                  }
		                  if ( lotsSpecialSymbols_prev == 0 ){
		                      if ( likeAddress == 1 ) return 96;
		                      if ( likeAddress == 0 ){
		                          if ( senderName_prev == 1 ){
		                              if ( emailPattern_after == 1 ) return 98;
		                              if ( emailPattern_after == 0 ){
		                                  if ( emailPattern_prev == 1 ) return 92;
		                                  if ( emailPattern_prev == 0 ){
		                                      if ( emailPattern == 0 ) return -94;
		                                      if ( emailPattern == 1 ) return 87;
		                                  }
		                              }
		                          }
		                          if ( senderName_prev == 0 ){
		                              if ( phoneNumberPattern_prev == 1 ){
		                                  if ( typicalSignWords_prev == 0 ){
		                                      if ( emailPattern_after == 0 ) return -96;
		                                      if ( emailPattern_after == 1 ) return 85;
		                                  }
		                                  if ( typicalSignWords_prev == 1 ){
		                                      if ( length <= 28 ) return 94;
		                                      if ( length > 28 ) return -89;
		                                  }
		                              }
		                              if ( phoneNumberPattern_prev == 0 ){
		                                  if ( senderName == 1 ){
		                                      if ( likeAName_after == 1 ) return -92;
		                                      if ( likeAName_after == 0 ){
		                                          if ( length <= 6 ) return 92;
		                                          if ( length > 6 ) return -77;
		                                      }
		                                  }
		                                  if ( senderName == 0 ){
		                                      if ( urlPattern_prev == 1 ){
		                                          if ( urlPattern_after == 1 ) return 96;
		                                          if ( urlPattern_after == 0 ){
		                                              if ( emailPattern_after == 0 ) return -95;
		                                              if ( emailPattern_after == 1 ) return 80;
		                                          }
		                                      }
		                                      if ( urlPattern_prev == 0 ){
		                                          if ( likeAddress_prev == 1 ) return 71;
		                                          if ( likeAddress_prev == 0 ){
		                                              if ( phoneNumberPattern == 1 ) 
		                                               {
		                                                if ( typicalReplyWords_after == 0 ) return -87;
		                                                if ( typicalReplyWords_after == 1 ) return 93;
		                                               }
		                                              if ( phoneNumberPattern == 0 ){
		                                                  if ( senderEmail_prev == 1 ){
		                                                      if ( length <= 20 ) return -95;
		                                                      if ( length > 20 ) return 83;
		                                                  }
		                                                  if ( senderEmail_prev == 0 ) 
		                                                   {
		                                                    if ( typicalSignWords_after == 0 ) return -99;
		                                                    if ( typicalSignWords_after == 1 ){
		                                                        if ( likeAName == 0 ){
		                                                            if ( likeAName_prev == 0 ) return -98;
		                                                            if ( likeAName_prev == 1 ){
		                                                                if ( length <= 6 ) return -96;
		                                                                if ( length > 6 ){
		                                                                    if ( length <= 7 ) return 88;
		                                                                    if ( length > 7 ) return -81;
		                                                                }
		                                                            }
		                                                        }
		                                                        if ( likeAName == 1 ){
		                                                            if ( likeAName_after == 1 ) return -96;
		                                                            if ( likeAName_after == 0 ){
		                                                                if ( startWithReplyMark_after == 1 ) return -85;
		                                                                if ( startWithReplyMark_after == 0 ){
		                                                                    if ( likeAName_prev == 0 ) return 95;
		                                                                    if ( likeAName_prev == 1 ) return -75;
		                                                                }
		                                                            }
		                                                        }
		                                                    }
		                                                   }
		                                              }
		                                          }
		                                      }
		                                  }
		                              }
		                          }
		                      }
		                  }
		              }
		          }
		      }
		  }

	// without length
		  /*
		  if ( typicalSignWords == 1 ){
		      if ( urlPattern_after == 1 ) return 99;
		      if ( urlPattern_after == 0 ){
		          if ( emailPattern_after == 1 ){
		              if ( typicalReplyWords_after == 1 ) return 99;
		              if ( typicalReplyWords_after == 0 ){
		                  if ( startWithReplyMark_after == 0 ) return 95;
		                  if ( startWithReplyMark_after == 1 ) return -80;
		              }
		          }
		          if ( emailPattern_after == 0 ){
		              if ( senderName_after == 1 ) return -96;
		              if ( senderName_after == 0 ){
		                  if ( phoneNumberPattern_prev == 1 ) return 99;
		                  if ( phoneNumberPattern_prev == 0 ){
		                      if ( emailPattern_prev == 1 ) return 99;
		                      if ( emailPattern_prev == 0 ){
		                          if ( phoneNumberPattern_after == 1 ) return 96;
		                          if ( phoneNumberPattern_after == 0 ){
		                              if ( urlPattern_prev == 1 ) return 97;
		                              if ( urlPattern_prev == 0 ){
		                                  if ( likeAddress == 1 ) return 93;
		                                  if ( likeAddress == 0 ){
		                                      if ( emailPattern == 0 ) return -99;
		                                      if ( emailPattern == 1 ) return 93;
		                                  }
		                              }
		                          }
		                      }
		                  }
		              }
		          }
		      }
		  }
		  if ( typicalSignWords == 0 ){
		      if ( urlPattern == 1 ) return 98;
		      if ( urlPattern == 0 ){
		          if ( phoneNumberPattern_after == 1 ){
		              if ( first1Sent == 1 ) return -92;
		              if ( first1Sent == 0 ){
		                  if ( typicalSignWords_after == 1 ) return 97;
		                  if ( typicalSignWords_after == 0 ){
		                      if ( senderEmail_after == 1 ) return 83;
		                      if ( senderEmail_after == 0 ){
		                          if ( startWithReplyMark_prev == 0 ) return -76;
		                          if ( startWithReplyMark_prev == 1 ) return 80;
		                      }
		                  }
		              }
		          }
		          if ( phoneNumberPattern_after == 0 ){
		              if ( likeAddress_after == 1 ) return 97;
		              if ( likeAddress_after == 0 ){
		                  if ( likeAddress == 1 ) return 96;
		                  if ( likeAddress == 0 ){
		                      if ( senderName_prev == 1 ){
		                          if ( emailPattern_after == 1 ) return 99;
		                          if ( emailPattern_after == 0 ){
		                              if ( emailPattern_prev == 1 ) return 92;
		                              if ( emailPattern_prev == 0 ){
		                                  if ( emailPattern == 0 ) return -94;
		                                  if ( emailPattern == 1 ) return 87;
		                              }
		                          }
		                      }
		                      if ( senderName_prev == 0 ){
		                          if ( phoneNumberPattern_prev == 1 ){
		                              if ( typicalSignWords_prev == 0 ){
		                                  if ( emailPattern_after == 0 ) return -96;
		                                  if ( emailPattern_after == 1 ) return 85;
		                              }
		                              if ( typicalSignWords_prev == 1 ){
		                                  if ( typicalReplyWords_after == 1 ) return 97;
		                                  if ( typicalReplyWords_after == 0 ){
		                                      if ( emailPattern_prev == 1 ) return 96;
		                                      if ( emailPattern_prev == 0 ){
		                                          if ( typicalReplyWords == 0 ) return -86;
		                                          if ( typicalReplyWords == 1 ) return 80;
		                                      }
		                                  }
		                              }
		                          }
		                          if ( phoneNumberPattern_prev == 0 ){
		                              if ( senderName == 1 ){
		                                  if ( likeAName_after == 0 ) return 92;
		                                  if ( likeAName_after == 1 ) return -92;
		                              }
		                              if ( senderName == 0 ){
		                                  if ( urlPattern_prev == 1 ){
		                                      if ( urlPattern_after == 1 ) return 96;
		                                      if ( urlPattern_after == 0 ){
		                                          if ( emailPattern_after == 0 ) return -95;
		                                          if ( emailPattern_after == 1 ) return 83;
		                                      }
		                                  }
		                                  if ( urlPattern_prev == 0 ){
		                                      if ( likeAddress_prev == 1 ) return 77;
		                                      if ( likeAddress_prev == 0 ){
		                                          if ( phoneNumberPattern == 1 ){
		                                              if ( typicalReplyWords_after == 0 ) return -88;
		                                              if ( typicalReplyWords_after == 1 ) return 93;
		                                          }
		                                          if ( phoneNumberPattern == 0 ){
		                                              if ( senderEmail_after == 1 ){
		                                                  if ( likeAName == 1 ) return 93;
		                                                  if ( likeAName == 0 ){
		                                                      if ( likeAName_prev == 0 ) return -98;
		                                                      if ( likeAName_prev == 1 ) 
		                                                       {
		                                                        if ( senderName_after == 0 ) return -75;
		                                                        if ( senderName_after == 1 ) return 88;
		                                                       }
		                                                  }
		                                              }
		                                              if ( senderEmail_after == 0 ) 
		                                               {
		                                                if ( typicalSignWords_after == 0 ) return -99;
		                                                if ( typicalSignWords_after == 1 ){
		                                                    if ( likeAName == 0 ) return -96;
		                                                    if ( likeAName == 1 ){
		                                                        if ( likeAName_after == 1 ) return -96;
		                                                        if ( likeAName_after == 0 ){
		                                                            if ( startWithReplyMark_after == 1 ) return -81;
		                                                            if ( startWithReplyMark_after == 0 ){
		                                                                if ( likeAName_prev == 0 ) return 93;
		                                                                if ( likeAName_prev == 1 ) return -75;
		                                                            }
		                                                        }
		                                                    }
		                                                }
		                                               }
		                                          }
		                                      }
		                                  }
		                              }
		                          }
		                      }
		                  }
		              }
		          }
		      }
		  }
		*/

		  
		return -100;
	}
	

}
