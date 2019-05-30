import React, {
  PropTypes,
  Component
} from 'react';

import {
  NativeModules
} from 'react-native';

const { ExoPlayerManager } = NativeModules;

module.exports = {
  ...ExoPlayerManager,
  play(data,callBack) {
  	var url = data.url;
  	var title = data.title === undefined ? null : data.title;
  	var subtitle = data.subtitle === undefined ? null : data.subtitle;
		callBack = typeof callBack == "function"? callBack:null;
    return ExoPlayerManager.showVideoPlayer(url,title,subtitle,callBack);
  }
}