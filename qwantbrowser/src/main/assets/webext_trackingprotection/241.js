"use strict";
(self["webpackChunkqwant_privacy_pilot"] = self["webpackChunkqwant_privacy_pilot"] || []).push([[241],{

/***/ 736:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "$": () => (/* binding */ backgroundPage)
/* harmony export */ });
/* harmony import */ var _adguard_tsurlfilter__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(4346);
/* harmony import */ var _adguard_tsurlfilter__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_adguard_tsurlfilter__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _utils_request_types__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(3485);
/* harmony import */ var _utils_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(5088);
/* harmony import */ var _common_common_script__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(1351);
/* harmony import */ var _browser__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(2273);
/* harmony import */ var _prefs__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(4847);
/* harmony import */ var _common_log__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(9224);
/* harmony import */ var _iconsCache__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(1719);
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */

/* eslint-disable max-len */








const backgroundPage = (() => {
  var _browser$webRequest8, _browser$webRequest9, _browser$webNavigatio;

  const runtime = function () {
    const onMessage = {
      addListener(callback) {
        // https://developer.chrome.com/extensions/runtime#event-onMessage
        _common_common_script__WEBPACK_IMPORTED_MODULE_3__/* .runtimeImpl.onMessage.addListener */ .V.onMessage.addListener((message, sender) => {
          const senderOverride = Object.create(null);

          if (sender.tab) {
            senderOverride.tab = (0,_utils_common__WEBPACK_IMPORTED_MODULE_2__/* .toTabFromChromeTab */ .bw)(sender.tab);
          }

          if (typeof sender.frameId !== 'undefined') {
            senderOverride.frameId = sender.frameId;
          }

          return callback(message, senderOverride);
        });
      }

    };
    return {
      setUninstallURL: _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.runtime.setUninstallURL */ .X.runtime.setUninstallURL,
      reload: _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.runtime.reload */ .X.runtime.reload,
      onMessage,
      onConnect: _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.runtime.onConnect */ .X.runtime.onConnect,

      get lastError() {
        return _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.runtime.lastError */ .X.runtime.lastError;
      }

    };
  }(); // Calculates scheme of this extension (e.g.: chrome-extension:// or moz-extension://)


  const extensionScheme = function () {
    const url = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.runtime.getURL */ .X.runtime.getURL('');

    if (!url) {
      return url;
    }

    const index = url.indexOf('://');

    if (index > 0) {
      return url.substring(0, index);
    }

    return url;
  }();
  /**
   * We are skipping requests to internal resources of extensions
   * (e.g. chrome-extension:// or moz-extension://... etc.)
   * @param details Request details
   * @returns {boolean}
   */


  function shouldSkipRequest(details) {
    return details.tabId === _utils_common__WEBPACK_IMPORTED_MODULE_2__/* .BACKGROUND_TAB_ID */ .HB && details.url.indexOf(extensionScheme) === 0;
  }

  const linkHelper = document.createElement('a');
  /**
   * Fixing request type:
   * https://code.google.com/p/chromium/issues/detail?id=410382
   *
   * @param url Request url
   * @returns String Fixed object type
   */

  function parseRequestTypeFromUrl(url) {
    linkHelper.href = url;
    const path = linkHelper.pathname;
    let requestType = (0,_utils_request_types__WEBPACK_IMPORTED_MODULE_1__/* .parseContentTypeFromUrlPath */ .O)(path);

    if (requestType === null) {
      // https://code.google.com/p/chromium/issues/detail?id=410382
      requestType = _utils_request_types__WEBPACK_IMPORTED_MODULE_1__/* .RequestTypes.OBJECT */ .l.OBJECT;
    }

    return requestType;
  }
  /**
   * An array of HTTP headers.
   * Each header is represented as a dictionary containing the keys name
   * and either value or binaryValue.
   * https://developer.chrome.com/extensions/webRequest#type-HttpHeaders
   * @typedef HttpHeaders
   * @type {Array.<{ name: String, value: String, binaryValue }>}
   */

  /**
   * @typedef RequestDetails
   * @type {Object}
   * @property {String} requestUrl - request url
   * @property {String} referrerUrl - the origin where the request was initiated
   * @property {{tabId: Number}} tab - request tab with tabId in property
   * @property {Number} requestId - the ID of the request
   * @property {Number} statusCode - standard HTTP status code
   * @property {String} method - standard HTTP method
   * @property {Number} frameId - ID of current frame. Frame IDs are unique within a tab.
   * @property {Number} requestFrameId - ID of frame where request is executed
   * @property {Number} requestType - request type {@link RequestTypes}
   * @property {HttpHeaders} [requestHeaders] - the HTTP request headers
   * @property {HttpHeaders} [responseHeaders] - the HTTP response headers
   * @property {String} redirectUrl - new URL in onBeforeRedirect event
   */

  /**
   * Argument passed to the webRequest event listener.
   * Every webRequest event listener has its own object with request details.
   * To learn more see https://developer.chrome.com/extensions/webRequest or
   * https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/webRequest
   * @typedef {Object} WebRequestDetails
   */

  /**
   * Transforms raw request details from different browsers into unified format
   * @param {WebRequestDetails} details raw webRequest details
   * @returns {RequestDetails} prepared request details
   */


  function getRequestDetails(details) {
    const tab = {
      tabId: details.tabId
    };
    /**
     * FF sends http instead of ws protocol at the http-listeners layer
     * Although this is expected, as the Upgrade request is indeed an HTTP request,
     * we use a chromium based approach in this case.
     */

    if (details.type === 'websocket' && details.url.indexOf('http') === 0) {
      details.url = details.url.replace(/^http(s)?:/, 'ws$1:');
    } // https://developer.chrome.com/extensions/webRequest#event-onBeforeRequest


    const requestDetails = {
      requestUrl: details.url,
      // request url
      url: details.url,
      tab,
      // request tab,
      tabId: details.tabId,
      requestId: details.requestId,
      statusCode: details.statusCode,
      method: details.method
    };
    let frameId = 0; // id of this frame (only for main_frame and sub_frame types)

    let requestFrameId = 0; // id of frame where request is executed

    let requestType; // request type

    switch (details.type) {
      case 'main_frame':
        frameId = 0;
        requestType = _utils_request_types__WEBPACK_IMPORTED_MODULE_1__/* .RequestTypes.DOCUMENT */ .l.DOCUMENT;
        break;

      case 'sub_frame':
        frameId = details.frameId; // for sub_frame use parentFrameId as id of frame that wraps this frame

        requestFrameId = details.parentFrameId;
        requestType = _utils_request_types__WEBPACK_IMPORTED_MODULE_1__/* .RequestTypes.SUBDOCUMENT */ .l.SUBDOCUMENT;
        break;

      default:
        requestFrameId = details.frameId;
        requestType = details.type.toUpperCase();
        break;
    } // Relate request to main_frame


    if (requestFrameId === -1) {
      requestFrameId = 0;
    }

    if (requestType === 'IMAGESET') {
      requestType = _utils_request_types__WEBPACK_IMPORTED_MODULE_1__/* .RequestTypes.IMAGE */ .l.IMAGE;
    }

    if (requestType === _utils_request_types__WEBPACK_IMPORTED_MODULE_1__/* .RequestTypes.OTHER */ .l.OTHER) {
      requestType = parseRequestTypeFromUrl(details.url);
    }
    /**
     * ping type is 'ping' in Chrome
     * but Firefox considers it as 'beacon'
     */


    if (requestType === 'BEACON') {
      requestType = _utils_request_types__WEBPACK_IMPORTED_MODULE_1__/* .RequestTypes.PING */ .l.PING;
    }
    /**
     * Use `OTHER` type as a fallback
     * https://github.com/AdguardTeam/AdguardBrowserExtension/issues/777
     */


    if (!(requestType in _utils_request_types__WEBPACK_IMPORTED_MODULE_1__/* .RequestTypes */ .l)) {
      requestType = _utils_request_types__WEBPACK_IMPORTED_MODULE_1__/* .RequestTypes.OTHER */ .l.OTHER;
    }

    requestDetails.frameId = frameId;
    requestDetails.requestFrameId = requestFrameId;
    requestDetails.requestType = requestType;

    if (details.requestHeaders) {
      requestDetails.requestHeaders = details.requestHeaders;
    }

    if (details.responseHeaders) {
      requestDetails.responseHeaders = details.responseHeaders;
    }

    if (details.requestBody) {
      requestDetails.requestBody = details.requestBody;
    }

    if (details.tabId === _utils_common__WEBPACK_IMPORTED_MODULE_2__/* .BACKGROUND_TAB_ID */ .HB) {
      // In case of background request, its details contains referrer url
      // Chrome uses `initiator`: https://developer.chrome.com/extensions/webRequest#event-onBeforeRequest
      // FF uses `originUrl`: https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/webRequest/onBeforeRequest#Additional_objects
      requestDetails.referrerUrl = details.originUrl || details.initiator;
    }

    requestDetails.originUrl = details.originUrl || details.initiator;
    requestDetails.thirdParty = _adguard_tsurlfilter__WEBPACK_IMPORTED_MODULE_0__.isThirdPartyRequest(requestDetails.requestUrl, requestDetails.originUrl);
    return requestDetails;
  }

  const onBeforeRequest = {
    /**
     * Wrapper for webRequest.onBeforeRequest event
     * It prepares requestDetails and passes them to the callback
     * @param callback callback function receives {RequestDetails} and handles event
     * @param {String} urls url match pattern https://developer.chrome.com/extensions/match_patterns
     * @param {string[]} types
     * @param {string[]} extraInfoSpecsDirty
     */
    addListener(callback, urls, types, extraInfoSpecsDirty) {
      var _browser$webRequest, _browser$webRequest$o;

      const filters = {};

      if (urls) {
        filters.urls = urls;
      }

      if (types) {
        filters.types = types;
      }

      const extraInfoSpec = ['blocking'];

      if (extraInfoSpecsDirty && extraInfoSpecsDirty.length > 0) {
        extraInfoSpecsDirty.forEach(spec => {
          extraInfoSpec.push(spec);
        });
      } // https://developer.chrome.com/extensions/webRequest#event-onBeforeRequest


      (_browser$webRequest = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest */ .X.webRequest) === null || _browser$webRequest === void 0 ? void 0 : (_browser$webRequest$o = _browser$webRequest.onBeforeRequest) === null || _browser$webRequest$o === void 0 ? void 0 : _browser$webRequest$o.addListener(details => {
        if (shouldSkipRequest(details)) {
          return;
        }

        const requestDetails = getRequestDetails(details);
        return callback(requestDetails);
      }, filters, extraInfoSpec);
    }

  };
  /**
   * Apply 'extraHeaders' option for request/response headers access/change. See:
   * https://groups.google.com/a/chromium.org/forum/#!topic/chromium-extensions/vYIaeezZwfQ
   * https://chromium-review.googlesource.com/c/chromium/src/+/1338165
   */

  const onBeforeSendHeadersExtraInfoSpec = ['requestHeaders', 'blocking'];
  const onHeadersReceivedExtraInfoSpec = ['responseHeaders', 'blocking'];

  if (_browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest */ .X.webRequest && typeof _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest.OnBeforeSendHeadersOptions */ .X.webRequest.OnBeforeSendHeadersOptions !== 'undefined' && _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest.OnBeforeSendHeadersOptions.hasOwnProperty */ .X.webRequest.OnBeforeSendHeadersOptions.hasOwnProperty('EXTRA_HEADERS')) {
    onBeforeSendHeadersExtraInfoSpec.push('extraHeaders');
  }

  if (_browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest */ .X.webRequest && typeof _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest.OnHeadersReceivedOptions */ .X.webRequest.OnHeadersReceivedOptions !== 'undefined' && _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest.OnHeadersReceivedOptions.hasOwnProperty */ .X.webRequest.OnHeadersReceivedOptions.hasOwnProperty('EXTRA_HEADERS')) {
    onHeadersReceivedExtraInfoSpec.push('extraHeaders');
  }

  const onHeadersReceived = {
    /**
     * Wrapper for webRequest.onHeadersReceived event
     * It prepares requestDetails and passes them to the callback
     * @param callback callback function receives {RequestDetails} and handles event
     * @param {Array.<String>} urls url match pattern https://developer.chrome.com/extensions/match_patterns
     */
    addListener(callback, urls) {
      var _browser$webRequest2, _browser$webRequest2$;

      (_browser$webRequest2 = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest */ .X.webRequest) === null || _browser$webRequest2 === void 0 ? void 0 : (_browser$webRequest2$ = _browser$webRequest2.onHeadersReceived) === null || _browser$webRequest2$ === void 0 ? void 0 : _browser$webRequest2$.addListener(details => {
        if (shouldSkipRequest(details)) {
          return;
        }

        const requestDetails = getRequestDetails(details);
        const result = callback(requestDetails);

        if (result) {
          return 'responseHeaders' in result ? {
            responseHeaders: result.responseHeaders
          } : {};
        }
      }, urls ? {
        urls
      } : {}, onHeadersReceivedExtraInfoSpec);
    }

  };
  const onBeforeSendHeaders = {
    /**
     * Wrapper for webRequest.onBeforeSendHeaders event
     * It prepares requestDetails and passes them to the callback
     * @param callback callback function receives {RequestDetails} and handles event
     * @param {Array.<String>} urls url match pattern https://developer.chrome.com/extensions/match_patterns
     */
    addListener(callback, urls) {
      var _browser$webRequest3, _browser$webRequest3$;

      let requestFilter = {};
      /**
       * Sometimes extraHeaders option of onBeforeSendHeaders handler is blocking network
       * https://github.com/AdguardTeam/AdguardBrowserExtension/issues/1634
       * https://github.com/AdguardTeam/AdguardBrowserExtension/issues/1644
       * https://bugs.chromium.org/p/chromium/issues/detail?id=938560
       * https://bugs.chromium.org/p/chromium/issues/detail?id=1075905
       * This issue was fixed in the Canary v85.0.4178.0 and would be fixed
       * in the Chrome with the same version
       * Until v85 we have decided to filter requests with types:
       * 'stylesheet', 'script', 'media'
       */

      if (_prefs__WEBPACK_IMPORTED_MODULE_5__/* .prefs.browser */ .D.browser === 'Chrome' && _prefs__WEBPACK_IMPORTED_MODULE_5__/* .prefs.chromeVersion */ .D.chromeVersion < 85) {
        const allTypes = ['main_frame', 'sub_frame', 'stylesheet', 'script', 'image', 'font', 'object', 'xmlhttprequest', 'ping', 'csp_report', 'media', 'websocket', 'other']; // this request types block requests, if use them with extraHeaders and blocking options

        const nonExtraHeadersTypes = ['stylesheet', 'script', 'media'];
        const extraHeadersTypes = allTypes.filter(type => !nonExtraHeadersTypes.includes(type)); // Assign instead of spread used because FF begin to support them from v55
        // https://caniuse.com/#feat=mdn-javascript_operators_spread_spread_in_object_literals

        requestFilter = Object.assign(requestFilter, {
          types: extraHeadersTypes
        });
      }

      if (urls) {
        // Assign instead of spread used because FF begin to support them from v55
        // https://caniuse.com/#feat=mdn-javascript_operators_spread_spread_in_object_literals
        requestFilter = Object.assign(requestFilter, {
          urls
        });
      }

      (_browser$webRequest3 = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest */ .X.webRequest) === null || _browser$webRequest3 === void 0 ? void 0 : (_browser$webRequest3$ = _browser$webRequest3.onBeforeSendHeaders) === null || _browser$webRequest3$ === void 0 ? void 0 : _browser$webRequest3$.addListener(details => {
        if (shouldSkipRequest(details)) {
          return;
        }

        const requestDetails = getRequestDetails(details);
        const result = callback(requestDetails);

        if (result) {
          return 'requestHeaders' in result ? {
            requestHeaders: result.requestHeaders
          } : {};
        }
      }, requestFilter, onBeforeSendHeadersExtraInfoSpec);
    }

  };
  const onResponseStarted = {
    /**
     * Wrapper for webRequest.onResponseStarted event
     * It prepares requestDetails and passes them to the callback
     * @param callback callback function receives {RequestDetails} and handles event
     * @param {String} urls url match pattern https://developer.chrome.com/extensions/match_patterns
     */
    addListener(callback, urls) {
      var _browser$webRequest4, _browser$webRequest4$;

      (_browser$webRequest4 = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest */ .X.webRequest) === null || _browser$webRequest4 === void 0 ? void 0 : (_browser$webRequest4$ = _browser$webRequest4.onResponseStarted) === null || _browser$webRequest4$ === void 0 ? void 0 : _browser$webRequest4$.addListener(details => {
        if (shouldSkipRequest(details)) {
          return;
        }

        const requestDetails = getRequestDetails(details);
        return callback(requestDetails);
      }, urls ? {
        urls
      } : {}, ['responseHeaders']);
    }

  };
  const onErrorOccurred = {
    /**
     * Wrapper for webRequest.onErrorOccurred event
     * It prepares requestDetails and passes them to the callback
     * @param callback callback function receives {RequestDetails} and handles event
     * @param {String} urls url match pattern https://developer.chrome.com/extensions/match_patterns
     */
    addListener(callback, urls) {
      var _browser$webRequest5, _browser$webRequest5$;

      (_browser$webRequest5 = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest */ .X.webRequest) === null || _browser$webRequest5 === void 0 ? void 0 : (_browser$webRequest5$ = _browser$webRequest5.onErrorOccurred) === null || _browser$webRequest5$ === void 0 ? void 0 : _browser$webRequest5$.addListener(details => {
        if (shouldSkipRequest(details)) {
          return;
        }

        const requestDetails = getRequestDetails(details);
        return callback(requestDetails);
      }, urls ? {
        urls
      } : {});
    }

  };
  const onCompleted = {
    /**
     * Wrapper for webRequest.onCompleted event
     * It prepares requestDetails and passes them to the callback
     * @param callback callback function receives {RequestDetails} and handles event
     * @param {String} urls url match pattern https://developer.chrome.com/extensions/match_patterns
     */
    addListener(callback, urls) {
      var _browser$webRequest6, _browser$webRequest6$;

      (_browser$webRequest6 = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest */ .X.webRequest) === null || _browser$webRequest6 === void 0 ? void 0 : (_browser$webRequest6$ = _browser$webRequest6.onCompleted) === null || _browser$webRequest6$ === void 0 ? void 0 : _browser$webRequest6$.addListener(details => {
        if (shouldSkipRequest(details)) {
          return;
        }

        const requestDetails = getRequestDetails(details);
        return callback(requestDetails);
      }, urls ? {
        urls
      } : {}, ['responseHeaders']);
    }

  };
  const onBeforeRedirect = {
    /**
     * Wrapper for webRequest.onBeforeRedirect event
     * It prepares requestDetails and passes them to the callback
     * @param callback callback function receives {RequestDetails} and handles event
     * @param {Array.<String>} urls url match pattern https://developer.chrome.com/extensions/match_patterns
     */
    addListener(callback, urls) {
      var _browser$webRequest7, _browser$webRequest7$;

      (_browser$webRequest7 = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest */ .X.webRequest) === null || _browser$webRequest7 === void 0 ? void 0 : (_browser$webRequest7$ = _browser$webRequest7.onBeforeRedirect) === null || _browser$webRequest7$ === void 0 ? void 0 : _browser$webRequest7$.addListener(details => {
        if (shouldSkipRequest(details)) {
          return;
        }

        const requestDetails = getRequestDetails(details);
        requestDetails.redirectUrl = details.redirectUrl;
        return callback(requestDetails);
      }, urls ? {
        urls
      } : {});
    }

  };
  /**
   * Gets URL of a file that belongs to our extension
   * https://developer.chrome.com/apps/runtime#method-getURL
   */
  // eslint-disable-next-line prefer-destructuring

  const getURL = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.runtime.getURL */ .X.runtime.getURL;
  const app = {
    /**
     * Extension ID
     */
    getId() {
      return _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.runtime.id */ .X.runtime.id;
    },

    /**
     * Gets extension scheme
     * @returns "chrome-extension" for Chrome," ms-browser-extension" for Edge
     */
    getUrlScheme() {
      const url = backgroundPage.getURL('test.html');
      const index = url.indexOf('://');
      return url.substring(0, index);
    },

    /**
     * Extension version
     */
    getVersion() {
      return _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.runtime.getManifest */ .X.runtime.getManifest().version;
    },

    /**
     * Extension UI locale
     */
    getLocale() {
      return _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.i18n.getUILanguage */ .X.i18n.getUILanguage();
    },

    /**
     * Returns extension's full url
     */
    getExtensionUrl() {
      const url = getURL('');
      return url.substring(0, url.length - 1);
    },

    /**
     * If referrer of request contains full url of extension,
     * then this request is considered as extension's own request
     * (e.g. request for filter downloading)
     * https://github.com/AdguardTeam/AdguardBrowserExtension/issues/1437
     * @param referrerUrl
     * @returns {boolean}
     */
    isOwnRequest(referrerUrl) {
      return referrerUrl && referrerUrl.indexOf(this.getExtensionUrl()) === 0;
    }

  };
  const webRequest = {
    onBeforeRequest,
    handlerBehaviorChanged: (_browser$webRequest8 = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest */ .X.webRequest) === null || _browser$webRequest8 === void 0 ? void 0 : _browser$webRequest8.handlerBehaviorChanged,
    onCompleted,
    onErrorOccurred,
    onHeadersReceived,
    onBeforeSendHeaders,
    onResponseStarted,
    onBeforeRedirect,
    webSocketSupported: _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest */ .X.webRequest && typeof _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest.ResourceType */ .X.webRequest.ResourceType !== 'undefined' && _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest.ResourceType.WEBSOCKET */ .X.webRequest.ResourceType.WEBSOCKET === 'websocket',
    filterResponseData: (_browser$webRequest9 = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webRequest */ .X.webRequest) === null || _browser$webRequest9 === void 0 ? void 0 : _browser$webRequest9.filterResponseData
  };
  const onCreatedNavigationTarget = {
    addListener(callback) {
      // https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/webNavigation/onCreatedNavigationTarget#Browser_compatibility
      if (!_browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webNavigation */ .X.webNavigation || typeof _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webNavigation.onCreatedNavigationTarget */ .X.webNavigation.onCreatedNavigationTarget === 'undefined') {
        return;
      }

      _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webNavigation.onCreatedNavigationTarget.addListener */ .X.webNavigation.onCreatedNavigationTarget.addListener(details => {
        if (details.tabId === _utils_common__WEBPACK_IMPORTED_MODULE_2__/* .BACKGROUND_TAB_ID */ .HB) {
          return;
        }

        callback({
          tabId: details.tabId,
          sourceTabId: details.sourceTabId,
          url: details.url
        });
      });
    }

  };
  const onCommitted = {
    /**
     * Wrapper for webNavigation.onCommitted event
     * It prepares webNavigation details and passes them to the callback
     * @param callback callback function receives object similar to {RequestDetails}
     * and handles event
     */
    addListener(callback) {
      if (!_browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webNavigation */ .X.webNavigation) return; // https://developer.chrome.com/extensions/webNavigation#event-onCommitted

      _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webNavigation.onCommitted.addListener */ .X.webNavigation.onCommitted.addListener(details => {
        // makes webNavigation.onCommitted details similar to webRequestDetails
        details.requestType = details.frameId === 0 ? _utils_request_types__WEBPACK_IMPORTED_MODULE_1__/* .RequestTypes.DOCUMENT */ .l.DOCUMENT : _utils_request_types__WEBPACK_IMPORTED_MODULE_1__/* .RequestTypes.SUBDOCUMENT */ .l.SUBDOCUMENT;
        details.tab = {
          tabId: details.tabId
        };
        details.requestUrl = details.url;
        callback(details);
      }, {
        url: [{
          urlPrefix: 'http'
        }, {
          urlPrefix: 'https'
        }]
      });
    }

  }; // https://developer.chrome.com/extensions/webNavigation

  const webNavigation = {
    onCreatedNavigationTarget,
    onCommitted,
    onDOMContentLoaded: (_browser$webNavigatio = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.webNavigation */ .X.webNavigation) === null || _browser$webNavigatio === void 0 ? void 0 : _browser$webNavigatio.onDOMContentLoaded
  };
  const browserActionSupported = typeof _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.browserAction.setIcon */ .X.browserAction.setIcon !== 'undefined';
  const browserAction = {
    /* eslint-disable-next-line no-unused-vars */
    async setBrowserAction(tab, icon, badge, badgeColor, title) {
      if (!browserActionSupported) {
        return;
      }

      const {
        tabId
      } = tab;

      const onIconReady = async () => {
        try {
          await _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.browserAction.setBadgeText */ .X.browserAction.setBadgeText({
            tabId,
            text: badge
          });
        } catch (e) {
          _common_log__WEBPACK_IMPORTED_MODULE_6__/* .log.debug */ .c.debug(new Error(e.message));
          return;
        }

        if (badge) {
          try {
            await _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.browserAction.setBadgeBackgroundColor */ .X.browserAction.setBadgeBackgroundColor({
              tabId,
              color: badgeColor
            });
          } catch (e) {
            _common_log__WEBPACK_IMPORTED_MODULE_6__/* .log.debug */ .c.debug(new Error(e.message));
          }
        } // title setup via manifest.json file
        // chrome.browserAction.setTitle({tabId: tabId, title: title});

      };
      /**
       * Workaround for MS Edge.
       * For some reason Edge changes the inner state of the "icon"
       * object and adds a tabId property inside.
       */


      delete icon.tabId;

      if (_browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.runtime.lastError */ .X.runtime.lastError) {
        return;
      }

      try {
        await _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.browserAction.setIcon */ .X.browserAction.setIcon({
          tabId,
          imageData: await (0,_iconsCache__WEBPACK_IMPORTED_MODULE_7__/* .getIconImageData */ .O)(icon)
        });
      } catch (e) {
        _common_log__WEBPACK_IMPORTED_MODULE_6__/* .log.debug */ .c.debug(new Error(e.message));
        return;
      }

      onIconReady();
    },

    setPopup() {// Do nothing. Popup is already installed in manifest file
    },

    resize() {// Do nothing
    },

    close() {// Do nothing
    }

  }; // eslint-disable-next-line prefer-destructuring
  // const contextMenus = browser.contextMenus;
  // eslint-disable-next-line prefer-destructuring

  const i18n = _browser__WEBPACK_IMPORTED_MODULE_4__/* .browser.i18n */ .X.i18n;
  return {
    runtime,
    getURL,
    app,
    webRequest,
    webNavigation,
    browserAction,
    // contextMenus,
    i18n
  };
})();

/***/ }),

/***/ 2273:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "X": () => (/* reexport default from dynamic */ webextension_polyfill__WEBPACK_IMPORTED_MODULE_0___default.a)
/* harmony export */ });
/* harmony import */ var webextension_polyfill__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(3679);
/* harmony import */ var webextension_polyfill__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(webextension_polyfill__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _windows__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(5802);


(0,_windows__WEBPACK_IMPORTED_MODULE_1__/* .patchWindows */ .x)((webextension_polyfill__WEBPACK_IMPORTED_MODULE_0___default()));


/***/ }),

/***/ 1719:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "O": () => (/* binding */ getIconImageData)
/* harmony export */ });
/* harmony import */ var core_js_modules_esnext_map_delete_all_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(3929);
/* harmony import */ var core_js_modules_esnext_map_delete_all_js__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_delete_all_js__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var core_js_modules_esnext_map_every_js__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(7851);
/* harmony import */ var core_js_modules_esnext_map_every_js__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_every_js__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var core_js_modules_esnext_map_filter_js__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(3633);
/* harmony import */ var core_js_modules_esnext_map_filter_js__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_filter_js__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var core_js_modules_esnext_map_find_js__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(1192);
/* harmony import */ var core_js_modules_esnext_map_find_js__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_find_js__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var core_js_modules_esnext_map_find_key_js__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(7515);
/* harmony import */ var core_js_modules_esnext_map_find_key_js__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_find_key_js__WEBPACK_IMPORTED_MODULE_4__);
/* harmony import */ var core_js_modules_esnext_map_includes_js__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(8034);
/* harmony import */ var core_js_modules_esnext_map_includes_js__WEBPACK_IMPORTED_MODULE_5___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_includes_js__WEBPACK_IMPORTED_MODULE_5__);
/* harmony import */ var core_js_modules_esnext_map_key_of_js__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(1480);
/* harmony import */ var core_js_modules_esnext_map_key_of_js__WEBPACK_IMPORTED_MODULE_6___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_key_of_js__WEBPACK_IMPORTED_MODULE_6__);
/* harmony import */ var core_js_modules_esnext_map_map_keys_js__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(9027);
/* harmony import */ var core_js_modules_esnext_map_map_keys_js__WEBPACK_IMPORTED_MODULE_7___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_map_keys_js__WEBPACK_IMPORTED_MODULE_7__);
/* harmony import */ var core_js_modules_esnext_map_map_values_js__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(5739);
/* harmony import */ var core_js_modules_esnext_map_map_values_js__WEBPACK_IMPORTED_MODULE_8___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_map_values_js__WEBPACK_IMPORTED_MODULE_8__);
/* harmony import */ var core_js_modules_esnext_map_merge_js__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(9283);
/* harmony import */ var core_js_modules_esnext_map_merge_js__WEBPACK_IMPORTED_MODULE_9___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_merge_js__WEBPACK_IMPORTED_MODULE_9__);
/* harmony import */ var core_js_modules_esnext_map_reduce_js__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(4473);
/* harmony import */ var core_js_modules_esnext_map_reduce_js__WEBPACK_IMPORTED_MODULE_10___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_reduce_js__WEBPACK_IMPORTED_MODULE_10__);
/* harmony import */ var core_js_modules_esnext_map_some_js__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(989);
/* harmony import */ var core_js_modules_esnext_map_some_js__WEBPACK_IMPORTED_MODULE_11___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_some_js__WEBPACK_IMPORTED_MODULE_11__);
/* harmony import */ var core_js_modules_esnext_map_update_js__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(7194);
/* harmony import */ var core_js_modules_esnext_map_update_js__WEBPACK_IMPORTED_MODULE_12___default = /*#__PURE__*/__webpack_require__.n(core_js_modules_esnext_map_update_js__WEBPACK_IMPORTED_MODULE_12__);













const cache = new Map();
/**
 * Download image and convert it to ImageData
 *
 * @param {Number} size - icon size in px
 * @param {String} url - icon url
 * @returns {ImageData}
 */

const loadImageData = (size, url) => {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.src = url;

    img.onload = () => {
      const canvas = document.createElement('canvas');
      document.documentElement.appendChild(canvas);
      canvas.width = size;
      canvas.height = size;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(img, 0, 0);
      const data = ctx.getImageData(0, 0, size, size);
      canvas.remove();
      resolve(data);
    };

    img.onerror = reject;
  });
};
/**
 * Get ImageData for specific url
 *
 * @param {Number} size - icon size in px
 * @param {String} url - icon url
 * @returns {[size: Number, imageData: ImageData]} - key-value entry for browserAction.setIcon 'imageData' property
 */


const getImageData = async (size, url) => {
  const imageData = cache.get(url);

  if (!imageData) {
    const data = await loadImageData(size, url);
    cache.set(url, data);
    return [size, data];
  }

  return [size, imageData];
};
/**
 * Match urls from browserAction.setIcon 'path' property with cached ImageData values
 * and return 'imageData' object for this action.
 *
 * see: https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/API/browserAction/setIcon
 *
 * @param {Object} path - browserAction.setIcon details 'path' property
 * @returns {Object} - browserAction.setIcon details 'imageData' property
 */


const getIconImageData = async path => {
  const imageDataEntriesPromises = Object.entries(path).map(([size, url]) => getImageData(size, url));
  const imageDataEntries = await Promise.all(imageDataEntriesPromises);
  const imageData = Object.fromEntries(imageDataEntries);
  return imageData;
};

/***/ }),

/***/ 4879:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "W": () => (/* binding */ tabsImpl)
/* harmony export */ });
/* harmony import */ var _browser__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(2273);
/* harmony import */ var _utils_common__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(5088);
/* harmony import */ var _prefs__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(4847);
/* harmony import */ var _utils_browser_utils__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(1654);
/* harmony import */ var _common_log__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(9224);
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */

/* eslint-disable max-len */





/**
 * Chromium tabs implementation
 * @type {{onCreated, onRemoved, onUpdated, onActivated, create, remove, activate, reload, sendMessage, getAll, getActive, fromChromeTab}}
 */

const tabsImpl = function () {
  /**
   * tabId parameter must be integer
   * @param tabId
   */
  function tabIdToInt(tabId) {
    return Number.parseInt(tabId, 10);
  }

  function logOperationError(operation, e) {
    _common_log__WEBPACK_IMPORTED_MODULE_4__/* .log.error */ .c.error('Error while executing operation{1}: {0}', e, operation ? ` '${operation}'` : '');
  }
  /**
   * Returns id of active tab
   * @returns {Promise<number|null>}
   */


  const getActive = async function () {
    /**
     * lastFocusedWindow parameter isn't supported by Opera
     * But seems currentWindow has the same effect in our case.
     * See for details:
     * https://developer.chrome.com/extensions/windows#current-window
     * https://dev.opera.com/extensions/tab-window/#accessing-the-current-tab
     * https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/tabs/query
     */
    let tabs;

    try {
      tabs = await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.query */ .X.tabs.query({
        currentWindow: true,
        active: true
      });
    } catch (e) {
      _common_log__WEBPACK_IMPORTED_MODULE_4__/* .log.debug */ .c.debug(new Error(e.message));
    }

    if (tabs && tabs.length > 0) {
      return tabs[0].id;
    }

    return null;
  }; // https://developer.chrome.com/extensions/tabs#event-onCreated


  const onCreatedChannel = _utils_common__WEBPACK_IMPORTED_MODULE_1__/* .utils.channels.newChannel */ .P6.channels.newChannel();
  _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.onCreated.addListener */ .X.tabs.onCreated.addListener(chromeTab => {
    onCreatedChannel.notify((0,_utils_common__WEBPACK_IMPORTED_MODULE_1__/* .toTabFromChromeTab */ .bw)(chromeTab));
  }); // https://developer.chrome.com/extensions/tabs#event-onCreated

  const onRemovedChannel = _utils_common__WEBPACK_IMPORTED_MODULE_1__/* .utils.channels.newChannel */ .P6.channels.newChannel();
  _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.onRemoved.addListener */ .X.tabs.onRemoved.addListener(tabId => {
    onRemovedChannel.notify(tabId);
  });
  const onUpdatedChannel = _utils_common__WEBPACK_IMPORTED_MODULE_1__/* .utils.channels.newChannel */ .P6.channels.newChannel(); // https://developer.chrome.com/extensions/tabs#event-onUpdated

  _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.onUpdated.addListener */ .X.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
    onUpdatedChannel.notify((0,_utils_common__WEBPACK_IMPORTED_MODULE_1__/* .toTabFromChromeTab */ .bw)(tab));
  }); // https://developer.chrome.com/extensions/tabs#event-onActivated

  const onActivatedChannel = _utils_common__WEBPACK_IMPORTED_MODULE_1__/* .utils.channels.newChannel */ .P6.channels.newChannel();
  _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.onActivated.addListener */ .X.tabs.onActivated.addListener(activeInfo => {
    onActivatedChannel.notify(activeInfo.tabId);
  }); // https://developer.chrome.com/extensions/windows#event-onFocusChanged

  _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.windows.onFocusChanged.addListener */ .X.windows.onFocusChanged.addListener(async windowId => {
    if (windowId === _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.windows.WINDOW_ID_NONE */ .X.windows.WINDOW_ID_NONE) {
      return;
    }

    const tabId = await getActive();

    if (tabId) {
      onActivatedChannel.notify(tabId);
    }
  });
  /**
   * Give focus to a window
   * @param tabId Tab identifier
   * @param windowId Window identifier
   */

  async function focusWindow(tabId, windowId) {
    /**
     * Updating already focused window produces bug in Edge browser
     * https://github.com/AdguardTeam/AdguardBrowserExtension/issues/675
     */
    const activeTabId = await getActive();

    if (activeTabId && tabId !== activeTabId) {
      // Focus window
      try {
        await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.windows.update */ .X.windows.update(windowId, {
          focused: true
        });
      } catch (e) {
        logOperationError(`Update window ${windowId}`, e);
      }
    }
  }
  /**
   * Creates new tab
   * @param createData
   */


  const create = async function (createData) {
    const {
      url,
      inNewWindow
    } = createData;
    const active = createData.active === true;

    if (createData.type === 'popup' // Does not work properly in Anniversary builds
    && !_utils_browser_utils__WEBPACK_IMPORTED_MODULE_3__/* .browserUtils.isEdgeBeforeCreatorsUpdate */ .z.isEdgeBeforeCreatorsUpdate() // Isn't supported by Android WebExt
    && !_prefs__WEBPACK_IMPORTED_MODULE_2__/* .prefs.mobile */ .D.mobile) {
      // https://developer.chrome.com/extensions/windows#method-create
      // https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/windows/create
      await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.windows.create */ .X.windows.create({
        url,
        type: 'popup',
        width: 1000,
        height: 650
      });
      return;
    }

    const isHttp = url.indexOf('http') === 0;

    async function onWindowFound(win) {
      // https://developer.chrome.com/extensions/tabs#method-create
      const chromeTab = await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.create */ .X.tabs.create({
        /**
         * In the Firefox browser for Android there is not concept of windows
         * There is only one window whole time
         * That's why if we try to provide windowId, method fails with error.
         */
        windowId: !_prefs__WEBPACK_IMPORTED_MODULE_2__/* .prefs.mobile */ .D.mobile ? win.id : undefined,
        url,
        active
      });

      if (active) {
        await focusWindow(chromeTab.id, chromeTab.windowId);
      }

      return (0,_utils_common__WEBPACK_IMPORTED_MODULE_1__/* .toTabFromChromeTab */ .bw)(chromeTab);
    }

    const onWindowCreatedWithTab = async win => {
      const [tab] = win.tabs;

      if (active) {
        await focusWindow(tab.id, tab.windowId);
      }

      return (0,_utils_common__WEBPACK_IMPORTED_MODULE_1__/* .toTabFromChromeTab */ .bw)(tab);
    };

    function isAppropriateWindow(win) {
      // We can't open not-http (e.g. 'chrome-extension://') urls in incognito mode
      return win.type === 'normal' && (isHttp || !win.incognito);
    }

    if (!inNewWindow) {
      // https://developer.chrome.com/extensions/windows#method-create
      // https://developer.chrome.com/extensions/windows#method-getLastFocused
      // https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/windows/create
      // https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/windows/getLastFocused
      const win = await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.windows.getLastFocused */ .X.windows.getLastFocused();

      if (isAppropriateWindow(win)) {
        return onWindowFound(win);
      } // https://github.com/AdguardTeam/AdguardBrowserExtension/issues/569


      const wins = await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.windows.getAll */ .X.windows.getAll({});

      if (wins) {
        for (let i = 0; i < wins.length; i += 1) {
          const win = wins[i];

          if (isAppropriateWindow(win)) {
            return onWindowFound(win);
          }
        }
      } // Create new window


      const newWin = await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.windows.create */ .X.windows.create();
      return onWindowFound(newWin);
    } // if inNewWindow
    // we open window with "url" to avoid empty new tab creation


    const newWin = await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.windows.create */ .X.windows.create({
      url
    });
    return onWindowCreatedWithTab(newWin);
  };

  const remove = async tabId => {
    // https://developer.chrome.com/extensions/tabs#method-remove
    // https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/tabs/remove
    try {
      await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.remove */ .X.tabs.remove(tabIdToInt(tabId));
    } catch (e) {
      return;
    }

    return tabId;
  };

  const activate = async function (tabId) {
    try {
      // https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/tabs/update
      const chromeTab = await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.update */ .X.tabs.update(tabIdToInt(tabId), {
        active: true
      });
      await focusWindow(tabId, chromeTab.windowId);
      return tabId;
    } catch (e) {
      logOperationError('Before tab update', e);
    }
  };
  /**
   * Sends message to tabs
   * @param tabId
   * @param message
   * @param options
   * @returns {Promise<*>}
   */


  const sendMessage = async (tabId, message, options) => {
    // https://developer.chrome.com/extensions/tabs#method-sendMessage
    // https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/tabs/sendMessage
    const args = [tabIdToInt(tabId), message];

    if (typeof options === 'object') {
      args.push(options);
    }

    try {
      const response = await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.sendMessage */ .X.tabs.sendMessage(...args);
      return response;
    } catch (e) {
      _common_log__WEBPACK_IMPORTED_MODULE_4__/* .log.debug */ .c.debug(e.message);
    }
  };

  const reload = async (tabId, url) => {
    if (url) {
      if (_utils_browser_utils__WEBPACK_IMPORTED_MODULE_3__/* .browserUtils.isEdgeBrowser */ .z.isEdgeBrowser()) {
        /**
         * For security reasons, in Firefox and Edge, this may not be a privileged URL.
         * So passing any of the following URLs will fail, with runtime.lastError being set to an error message:
         * chrome: URLs
         * javascript: URLs
         * data: URLs
         * privileged about: URLs (for example, about:config, about:addons, about:debugging).
         *
         * Non-privileged URLs (about:home, about:newtab, about:blank) are allowed.
         *
         * So we use a content script instead.
         */

        /**
         * Content script may not have been loaded at this point yet.
         * https://github.com/AdguardTeam/AdguardBrowserExtension/issues/580
         */
        setTimeout(() => {
          sendMessage(tabId, {
            type: 'update-tab-url',
            url
          });
        }, 100);
      } else {
        try {
          await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.update */ .X.tabs.update(tabIdToInt(tabId), {
            url
          });
        } catch (e) {
          logOperationError('Tab update', e);
        }
      } // https://developer.chrome.com/extensions/tabs#method-reload
      // https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/tabs/reload#Browser_compatibility

    } else if (_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.reload */ .X.tabs.reload) {
      try {
        await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.reload */ .X.tabs.reload(tabIdToInt(tabId), {
          bypassCache: true
        });
      } catch (e) {
        logOperationError('Tab reload', e);
      }
    } else {
      // Reload page without cache via content script
      sendMessage(tabId, {
        type: 'no-cache-reload'
      });
    }
  };

  const getAll = async () => {
    // https://developer.chrome.com/extensions/tabs#method-query
    // https://developer.mozilla.org/en-US/Add-ons/WebExtensions/API/tabs/query
    const chromeTabs = await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.query */ .X.tabs.query({});
    const result = [];

    for (let i = 0; i < chromeTabs.length; i += 1) {
      const chromeTab = chromeTabs[i];
      result.push((0,_utils_common__WEBPACK_IMPORTED_MODULE_1__/* .toTabFromChromeTab */ .bw)(chromeTab));
    }

    return result;
  };
  /**
   * Gets tab by id
   * @param tabId Tab identifier
   */


  const get = async tabId => {
    try {
      const chromeTab = await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.get */ .X.tabs.get(tabIdToInt(tabId));
      return (0,_utils_common__WEBPACK_IMPORTED_MODULE_1__/* .toTabFromChromeTab */ .bw)(chromeTab);
    } catch (e) {
      logOperationError('Get tab', e);
    }
  };
  /**
   * Updates tab url
   * @param {number} tabId
   * @param {string} url
   */


  const updateUrl = async (tabId, url) => {
    if (tabId === 0) {
      return;
    }

    try {
      await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.update */ .X.tabs.update(tabId, {
        url
      });
    } catch (e) {
      _common_log__WEBPACK_IMPORTED_MODULE_4__/* .log.error */ .c.error(new Error(e.message));
    }
  };
  /**
   * True if `browser.tabs.insertCSS` supports `cssOrigin: "user"`.
   */


  let userCSSSupport = true;
  /**
   * Inserts CSS using the `browser.tabs.insertCSS` under the hood.
   * This method always injects CSS using `runAt: document_start`/
   *
   * @param {number} tabId Tab id or null if you want to inject into the active tab
   * @param {number} requestFrameId Target frame id (CSS will be inserted into that frame)
   * @param {number} code CSS code to insert
   */

  const insertCssCode = !_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.insertCSS */ .X.tabs.insertCSS ? undefined : async (tabId, requestFrameId, code) => {
    const injectDetails = {
      code,
      runAt: 'document_start',
      frameId: requestFrameId,
      matchAboutBlank: true
    };

    if (userCSSSupport) {
      // If this is set for not supporting browser, it will throw an error.
      injectDetails.cssOrigin = 'user';
    }

    try {
      await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.insertCSS */ .X.tabs.insertCSS(tabId, injectDetails);
    } catch (e) {
      // e.message in edge is undefined
      const errorMessage = e.message || e; // Some browsers do not support user css origin // TODO which one?

      if (/\bcssOrigin\b/.test(errorMessage)) {
        userCSSSupport = false;
      }
    }
  };
  /**
   * Executes the specified JS code using `browser.tabs.executeScript` under the hood.
   * This method forces `runAt: document_start`.
   *
   * @param {number} tabId Tab id or null if you want to inject into the active tab
   * @param {requestFrameId} requestFrameId Target frame id (script will be injected into that frame)
   * @param {requestFrameId} code Javascript code to execute
   */

  const executeScriptCode = !_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.executeScript */ .X.tabs.executeScript ? undefined : async (tabId, requestFrameId, code) => {
    try {
      await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.executeScript */ .X.tabs.executeScript(tabId, {
        code,
        frameId: requestFrameId,
        runAt: 'document_start',
        matchAboutBlank: true
      });
    } catch (e) {
      _common_log__WEBPACK_IMPORTED_MODULE_4__/* .log.debug */ .c.debug(new Error(e.message));
    }
  };
  /**
   * Executes the specified javascript file in the top frame of the specified tab.
   * This method forces `runAt: document_start`.
   *
   * @param {number} tabId Tab id or null if you want to inject into the active tab
   * @param {Object} options
   * @param {string} options.file - Path to the javascript file
   * @param {number} [options.frameId=0] - id of the frame, default to the 0;
   * @param {function} callback Called when the script injection is complete
   */

  const executeScriptFile = !_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.executeScript */ .X.tabs.executeScript ? undefined : async (tabId, options) => {
    const {
      file,
      frameId = 0
    } = options;
    const executeScriptOptions = {
      file,
      runAt: 'document_start'
    }; // Chrome 49 throws an exception if browser.tabs.executeScript is called
    // with a frameId equal to 0

    if (frameId !== 0) {
      executeScriptOptions.frameId = frameId;
    }

    try {
      await _browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.tabs.executeScript */ .X.tabs.executeScript(tabId, executeScriptOptions);
    } catch (e) {
      _common_log__WEBPACK_IMPORTED_MODULE_4__/* .log.debug */ .c.debug(new Error(e.message));
    }
  };
  return {
    onCreated: onCreatedChannel,
    onRemoved: onRemovedChannel,
    onUpdated: onUpdatedChannel,
    onActivated: onActivatedChannel,
    create,
    remove,
    activate,
    reload,
    sendMessage,
    getAll,
    getActive,
    get,
    updateUrl,
    insertCssCode,
    executeScriptCode,
    executeScriptFile
  };
}();

/***/ }),

/***/ 5802:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "x": () => (/* binding */ patchWindows)
/* harmony export */ });
/* eslint-disable no-unused-vars */

/**
 * This function patches if necessary browser.windows implementation for Firefox for Android
 */
const patchWindows = function (browser) {
  // Make compatible with Android WebExt
  if (typeof browser.windows === 'undefined') {
    browser.windows = function () {
      const defaultWindow = {
        id: 1,
        type: 'normal'
      };
      const emptyListener = {
        addListener() {// Doing nothing
        }

      };

      const create = function (createData) {
        return Promise.resolve(defaultWindow);
      };

      const update = function (windowId, data) {
        return Promise.resolve();
      };

      const getAll = function (query) {
        return Promise.resolve(defaultWindow);
      };

      const getLastFocused = function () {
        return Promise.resolve(defaultWindow);
      };

      return {
        onCreated: emptyListener,
        onRemoved: emptyListener,
        onFocusChanged: emptyListener,
        create,
        update,
        getAll,
        getLastFocused
      };
    }();
  }
};

/***/ }),

/***/ 4847:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "D": () => (/* binding */ prefs)
/* harmony export */ });
/* harmony import */ var _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(2273);
/* harmony import */ var _utils_lazy__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(1255);
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */


/**
 * Extension global preferences.
 */

const prefs = (() => {
  const Prefs = {
    get mobile() {
      return (0,_utils_lazy__WEBPACK_IMPORTED_MODULE_1__/* .lazyGet */ .$)(Prefs, 'mobile', () => navigator.userAgent.indexOf('Android') >= 0);
    },

    get platform() {
      return (0,_utils_lazy__WEBPACK_IMPORTED_MODULE_1__/* .lazyGet */ .$)(Prefs, 'platform', () => window.browser ? 'firefox' : 'chromium');
    },

    get browser() {
      return (0,_utils_lazy__WEBPACK_IMPORTED_MODULE_1__/* .lazyGet */ .$)(Prefs, 'browser', () => {
        let browser;
        let {
          userAgent
        } = navigator;
        userAgent = userAgent.toLowerCase();

        if (userAgent.indexOf('yabrowser') >= 0) {
          browser = 'YaBrowser';
        } else if (userAgent.indexOf('edge') >= 0) {
          browser = 'Edge';
        } else if (userAgent.indexOf('edg') >= 0) {
          browser = 'EdgeChromium';
        } else if (userAgent.indexOf('opera') >= 0 || userAgent.indexOf('opr') >= 0) {
          browser = 'Opera';
        } else if (userAgent.indexOf('firefox') >= 0) {
          browser = 'Firefox';
        } else {
          browser = 'Chrome';
        }

        return browser;
      });
    },

    get chromeVersion() {
      return (0,_utils_lazy__WEBPACK_IMPORTED_MODULE_1__/* .lazyGet */ .$)(Prefs, 'chromeVersion', () => {
        const match = /\sChrome\/(\d+)\./.exec(navigator.userAgent);
        return match === null ? null : Number.parseInt(match[1], 10);
      });
    },

    get firefoxVersion() {
      return (0,_utils_lazy__WEBPACK_IMPORTED_MODULE_1__/* .lazyGet */ .$)(Prefs, 'firefoxVersion', () => {
        const match = /\sFirefox\/(\d+)\./.exec(navigator.userAgent);
        return match === null ? null : Number.parseInt(match[1], 10);
      });
    },

    /**
     * https://msdn.microsoft.com/ru-ru/library/hh869301(v=vs.85).aspx
     * @returns {*}
     */
    get edgeVersion() {
      return (0,_utils_lazy__WEBPACK_IMPORTED_MODULE_1__/* .lazyGet */ .$)(Prefs, 'edgeVersion', function () {
        if (this.browser === 'Edge') {
          const {
            userAgent
          } = navigator;
          const i = userAgent.indexOf('Edge/');

          if (i < 0) {
            return {
              rev: 0,
              build: 0
            };
          }

          const version = userAgent.substring(i + 'Edge/'.length);
          const parts = version.split('.');
          return {
            rev: Number.parseInt(parts[0], 10),
            build: Number.parseInt(parts[1], 10)
          };
        }
      });
    },

    /**
     * Makes sense in case of FF add-on only
     */
    speedupStartup() {
      return false;
    },

    get ICONS() {
      return (0,_utils_lazy__WEBPACK_IMPORTED_MODULE_1__/* .lazyGet */ .$)(Prefs, 'ICONS', () => ({
        ICON_GREEN: {
          '19': _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.runtime.getURL */ .X.runtime.getURL('assets/icons/green-19.png'),
          '38': _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.runtime.getURL */ .X.runtime.getURL('assets/icons/green-38.png')
        },
        ICON_GRAY: {
          '19': _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.runtime.getURL */ .X.runtime.getURL('assets/icons/gray-19.png'),
          '38': _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.runtime.getURL */ .X.runtime.getURL('assets/icons/gray-38.png')
        }
      }));
    },

    // interval 60 seconds in Firefox is set so big due to excessive IO operations on every storage save
    // https://github.com/AdguardTeam/AdguardBrowserExtension/issues/1006
    get statsSaveInterval() {
      return this.browser === 'Firefox' ? 1000 * 60 : 1000;
    }

  };
  /**
   * Collect browser specific features here
   */

  Prefs.features = function () {
    // Get the global extension object (browser for FF, chrome for Chromium)
    const browser = window.browser || window.chrome;
    const responseContentFilteringSupported = typeof browser !== 'undefined' && typeof browser.webRequest !== 'undefined' && typeof browser.webRequest.filterResponseData !== 'undefined';
    const canUseInsertCSSAndExecuteScript = // Blink engine based browsers
    (Prefs.browser === 'Chrome' || Prefs.browser === 'Opera' || Prefs.browser === 'YaBrowser' || Prefs.browser === 'EdgeChromium' // Support for tabs.insertCSS and tabs.executeScript on chrome
    // requires chrome version above or equal to 39,
    // as per documentation: https://developers.chrome.com/extensions/tabs
    // But due to a bug, it requires version >= 50
    // https://bugs.chromium.org/p/chromium/issues/detail?id=63979
    ) && Prefs.chromeVersion >= 50 || Prefs.browser === 'Firefox' && typeof browser !== 'undefined' && typeof browser.tabs !== 'undefined' && typeof browser.tabs.insertCSS !== 'undefined'; // Edge browser does not support `runAt` in options of tabs.insertCSS
    // and tabs.executeScript

    return {
      responseContentFilteringSupported,
      canUseInsertCSSAndExecuteScript,
      hasBackgroundTab: typeof browser !== 'undefined' // Background requests have sense only in case of webext

    };
  }();

  return Prefs;
})();

/***/ }),

/***/ 1315:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "t": () => (/* reexport safe */ _rules_storage_ABSTRACT_BROWSERS___WEBPACK_IMPORTED_MODULE_0__.Z)
/* harmony export */ });
/* harmony import */ var _rules_storage_ABSTRACT_BROWSERS___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(7088);
// !IMPORTANT!
// './rules-storage.__ABSTRACT_BROWSER__' is replaced during webpack compilation
// with NormalModuleReplacementPlugin to proper browser implementation
// './rules-storage.chrome' or ./rules-storage.firefox



/***/ }),

/***/ 9869:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Z": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony import */ var _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(2273);
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Filter rules storage implementation
 */

const chromeRulesStorageImpl = (() => {
  const read = async path => {
    const results = await _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.storage.local.get */ .X.storage.local.get(path);
    let lines = [];

    if (results && results[path] instanceof Array) {
      lines = results[path];
    }

    return lines;
  };

  const write = async (path, data) => {
    const item = {};
    item[path] = data;
    await _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.storage.local.set */ .X.storage.local.set(item);
  };

  const remove = async path => {
    await _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.storage.local.remove */ .X.storage.local.remove(path);
  };

  return {
    read,
    write,
    remove
  };
})();

/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (chromeRulesStorageImpl);

/***/ }),

/***/ 7088:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Z": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony import */ var _rules_storage_chrome__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(9869);
/* harmony import */ var _common_log__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(9224);
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */
// We use chrome rules storage implementation as fallback as it based on storage.local


/**
 * Filter rules storage implementation. Based on the indexedDB
 *
 * We have to use indexedDB instead of browser.storage.local due to some problems with the latest one.
 * browser.storage.local has high memory and disk utilization.
 *
 * https://bugzilla.mozilla.org/show_bug.cgi?id=1371255
 * https://github.com/AdguardTeam/AdguardBrowserExtension/issues/892
 */

const firefoxRulesStorageImpl = function (initialAPI) {
  const STORAGE_NAME = 'AdguardRulesStorage';
  let database;

  function onError(error) {
    _common_log__WEBPACK_IMPORTED_MODULE_1__/* .log.error */ .c.error('Adguard rulesStorage error: {0}', error.error || error);
  }
  /**
   * Gets value from the database by key
   */


  function getFromDatabase(key) {
    return new Promise((resolve, reject) => {
      const transaction = database.transaction(STORAGE_NAME);
      const table = transaction.objectStore(STORAGE_NAME);
      const request = table.get(key);

      const eventHandler = event => {
        const request = event.target;

        if (request.error) {
          reject(request.error);
          return;
        }

        let lines = [];
        const {
          result
        } = request;

        if (result && result.value) {
          lines = result.value.split(/\r?\n/);
        }

        resolve(lines);
      };

      request.onsuccess = eventHandler;
      request.onerror = eventHandler;
    });
  }
  /**
   * Puts key and value to the database
   */


  function putToDatabase(key, value) {
    return new Promise((resolve, reject) => {
      const transaction = database.transaction(STORAGE_NAME, 'readwrite');
      const table = transaction.objectStore(STORAGE_NAME);
      const request = table.put({
        key,
        value: value.join('\n')
      });

      const eventHandler = event => {
        const request = event.target;

        if (request.error) {
          reject(request.error);
        } else {
          resolve();
        }
      };

      request.onsuccess = eventHandler;
      request.onerror = eventHandler;
    });
  }
  /**
   * Deletes value from the database
   */


  function deleteFromDatabase(key) {
    return new Promise((resolve, reject) => {
      const transaction = database.transaction(STORAGE_NAME, 'readwrite');
      const table = transaction.objectStore(STORAGE_NAME);
      const request = table.delete(key);

      const eventHandler = event => {
        const request = event.target;

        if (request.error) {
          reject(request.error);
        } else {
          resolve();
        }
      };

      request.onsuccess = eventHandler;
      request.onerror = eventHandler;
    });
  }
  /**
   * Read rules
   * @param path Path to rules
   */


  const read = async path => {
    const result = await getFromDatabase(path);
    return result;
  };
  /**
   * Writes rules
   * @param path Path to rules
   * @param data Data to write (Array)
   */


  const write = async (path, data) => {
    await putToDatabase(path, data);
  };
  /**
   * Removes rules
   * @param path Path to rules
   */


  const remove = async path => {
    await deleteFromDatabase(path);
  };
  /**
   * We can detect whether IndexedDB was initialized or not only in an async way
   */


  const init = () => new Promise(resolve => {
    // Failed in private browsing mode.
    const request = indexedDB.open(STORAGE_NAME, 1);

    request.onupgradeneeded = function (ev) {
      database = ev.target.result;
      database.onerror = onError;
      database.onabort = onError; // DB doesn't exist => creates new storage

      const table = database.createObjectStore(STORAGE_NAME, {
        keyPath: 'key'
      });
      table.createIndex('value', 'value', {
        unique: false
      });
    };

    request.onsuccess = function (ev) {
      database = ev.target.result;
      database.onerror = onError;
      database.onabort = onError;
      resolve(api);
    };

    const onRequestError = function () {
      onError(this.error); // Fallback to the browser.storage API

      resolve(initialAPI);
    };

    request.onerror = onRequestError;
    request.onblocked = onRequestError;
  });

  const api = {
    read,
    write,
    remove,
    init,

    /**
     * IndexedDB isn't initialized in the private mode.
     * In this case we should switch implementation to the browser.storage (see init method)
     * This flag helps us to understand which implementation is used now (see update-service.js for example)
     */
    isIndexedDB: true
  };
  return api;
}(_rules_storage_chrome__WEBPACK_IMPORTED_MODULE_0__/* ["default"] */ .Z);

/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (firefoxRulesStorageImpl);

/***/ }),

/***/ 7789:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "X": () => (/* binding */ localStorage)
/* harmony export */ });
/* unused harmony export rulesStorage */
/* harmony import */ var _common_log__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(9224);
/* harmony import */ var _utils_local_storage__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(5131);
/* harmony import */ var _rules_storage__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(1315);
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */



/**
 * This class manages local storage
 */

const localStorage = function (localStorageImpl) {
  const getItem = function (key) {
    return localStorageImpl.getItem(key);
  };

  const setItem = function (key, value) {
    try {
      localStorageImpl.setItem(key, value);
    } catch (ex) {
      _common_log__WEBPACK_IMPORTED_MODULE_0__/* .log.error */ .c.error(`Error while saving item ${key} to the localStorage: ${ex}`);
    }
  };

  const removeItem = function (key) {
    localStorageImpl.removeItem(key);
  };

  const hasItem = function (key) {
    return localStorageImpl.hasItem(key);
  };

  const init = async function () {
    if (typeof localStorageImpl.init === 'function') {
      await localStorageImpl.init();
    }
  };

  const isInitialized = function () {
    // WebExtension storage has async initialization
    if (typeof localStorageImpl.isInitialized === 'function') {
      return localStorageImpl.isInitialized();
    }

    return true;
  };

  return {
    getItem,
    setItem,
    removeItem,
    hasItem,
    init,
    isInitialized
  };
}(_utils_local_storage__WEBPACK_IMPORTED_MODULE_1__/* .localStorageImpl */ .i);
/**
 * This class manages storage for filters.
 */

const rulesStorage = (rulesStorageImpl => {
  function getFilePath(filterId) {
    return `filterrules_${filterId}.txt`;
  }
  /**
   * Loads filter from the storage
   *
   * @param filterId  Filter identifier
   */


  const read = async filterId => {
    const filePath = getFilePath(filterId);
    let rules;

    try {
      rules = await rulesStorageImpl.read(filePath);
    } catch (e) {
      _common_log__WEBPACK_IMPORTED_MODULE_0__/* .log.error */ .c.error(`Error while reading rules from file ${filePath} cause: ${e}`);
    }

    return rules;
  };
  /**
   * Saves filter rules to storage
   *
   * @param filterId      Filter identifier
   * @param filterRules   Filter rules
   */


  const write = async (filterId, filterRules) => {
    const filePath = getFilePath(filterId);

    try {
      await rulesStorageImpl.write(filePath, filterRules);
    } catch (e) {
      _common_log__WEBPACK_IMPORTED_MODULE_0__/* .log.error */ .c.error(`Error writing filters to file ${filePath}. Cause: ${e}`);
    }
  };
  /**
   * Removes filter from storage
   * @param filterId
   */


  const remove = async filterId => {
    const filePath = getFilePath(filterId);

    try {
      await rulesStorageImpl.remove(filePath);
    } catch (e) {
      _common_log__WEBPACK_IMPORTED_MODULE_0__/* .log.error */ .c.error(`Error removing filter ${filePath}. Cause: ${e}`);
    }
  };
  /**
   * IndexedDB implementation of the rules storage requires async initialization.
   * Also in some cases IndexedDB isn't supported, so we have to replace implementation
   * with the browser.storage
   */


  const init = async () => {
    if (typeof rulesStorageImpl.init === 'function') {
      const api = await rulesStorageImpl.init();
      rulesStorageImpl = api;
    }
  };

  return {
    read,
    write,
    remove,
    init
  };
})(_rules_storage__WEBPACK_IMPORTED_MODULE_2__/* .rulesStorageImpl */ .t);

/***/ }),

/***/ 6458:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "n": () => (/* binding */ tabsApi)
/* harmony export */ });
/* harmony import */ var _utils_common__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(5088);
/* harmony import */ var _extension_api_tabs__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(4879);
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */



const tabsApi = (tabsImpl => {
  const tabs = Object.create(null); // Fired when a tab is created. Note that the tab's URL may not be set at the time
  // this event fired, but you can listen to onUpdated events to be notified when a URL is set.

  const onCreatedChannel = _utils_common__WEBPACK_IMPORTED_MODULE_0__/* .utils.channels.newChannel */ .P6.channels.newChannel(); // Fired when a tab is closed.

  const onRemovedChannel = _utils_common__WEBPACK_IMPORTED_MODULE_0__/* .utils.channels.newChannel */ .P6.channels.newChannel(); // Fired when a tab is updated.

  const onUpdatedChannel = _utils_common__WEBPACK_IMPORTED_MODULE_0__/* .utils.channels.newChannel */ .P6.channels.newChannel(); // Fires when the active tab in a window changes.

  const onActivatedChannel = _utils_common__WEBPACK_IMPORTED_MODULE_0__/* .utils.channels.newChannel */ .P6.channels.newChannel();
  /**
   * Saves tab to collection and notify listeners
   * @param aTab
   */

  function onTabCreated(aTab) {
    const tab = tabs[aTab.tabId];

    if (tab) {
      // Tab has been already synchronized
      return;
    }

    tabs[aTab.tabId] = aTab;
    onCreatedChannel.notify(aTab);
  } // Synchronize opened tabs


  (async () => {
    const aTabs = await tabsImpl.getAll();

    for (let i = 0; i < aTabs.length; i += 1) {
      const aTab = aTabs[i];
      tabs[aTab.tabId] = aTab;
    }
  })();

  tabsImpl.onCreated.addListener(onTabCreated);
  tabsImpl.onRemoved.addListener(tabId => {
    const tab = tabs[tabId];

    if (tab) {
      onRemovedChannel.notify(tab);
      delete tabs[tabId];
    }
  });
  tabsImpl.onUpdated.addListener(aTab => {
    const tab = tabs[aTab.tabId];

    if (tab) {
      tab.url = aTab.url;
      tab.title = aTab.title;
      tab.status = aTab.status; // If the tab was updated it means that it wasn't used to send requests in the background

      tab.synthetic = false;
      onUpdatedChannel.notify(tab);
    }
  });
  tabsImpl.onActivated.addListener(tabId => {
    const tab = tabs[tabId];

    if (tab) {
      onActivatedChannel.notify(tab);
    }
  }); // --------- Actions ---------
  // Creates a new tab.

  const create = async details => {
    return tabsImpl.create(details);
  }; // Closes tab.


  const remove = async tabId => {
    return tabsImpl.remove(tabId);
  }; // Activates tab (Also makes tab's window in focus).


  const activate = function (tabId) {
    return tabsImpl.activate(tabId);
  }; // Reloads tab.


  const reload = async (tabId, url) => {
    await tabsImpl.reload(tabId, url);
  }; // Updates tab url


  const updateUrl = (tabId, url) => {
    tabsImpl.updateUrl(tabId, url);
  }; // Sends message to tab


  const sendMessage = function (tabId, message, options) {
    return tabsImpl.sendMessage(tabId, message, options);
  };
  /**
   * Sometimes chrome does not return url and title on tab update events,
   * but returns tabs with urls when tabs are requested by tabs api
   * That is why during getting tabs we sync their urls with actual values
   */


  const syncTabs = (targetTabs, actualTab) => {
    const {
      tabId
    } = actualTab;
    const tab = targetTabs[tabId];

    if (!tab) {
      targetTabs[tabId] = actualTab;
      return actualTab;
    }

    if (!tab.url && actualTab.url) {
      tab.url = actualTab.url;
    }

    if (!tab.title && actualTab.title) {
      tab.title = actualTab.title;
    } // update tab state in the target tabs array


    targetTabs[tabId] = tab;
    return tab;
  }; // Gets all opened tabs


  const getAll = async () => {
    const aTabs = await tabsImpl.getAll();
    const result = [];

    for (let i = 0; i < aTabs.length; i += 1) {
      const aTab = aTabs[i];
      const tab = syncTabs(tabs, aTab);
      result.push(tab);
    }

    return result;
  }; // Calls callback with each tab


  const forEach = function (callback) {
    (async () => {
      const aTabs = await tabsImpl.getAll();

      for (let i = 0; i < aTabs.length; i += 1) {
        const aTab = aTabs[i];
        let tab = tabs[aTab.tabId];

        if (!tab) {
          // Synchronize state
          tabs[aTab.tabId] = aTab;
          tab = aTab;
        }

        callback(tab);
      }
    })();
  }; // Gets active tab


  const getActive = async tabId => {
    if (!tabId) {
      tabId = await tabsImpl.getActive();
    }

    if (!tabId) {
      return null;
    }

    let tab = tabs[tabId];

    if (tab) {
      if (!tab.url || !tab.title) {
        const aTab = await tabsImpl.get(tabId);

        if (aTab) {
          syncTabs(tabs, aTab);
        }
      }

      return tab;
    } // Tab not found in the local state, but we are sure that this tab exists. Sync...
    // TODO[Edge]: Relates to Edge Bug https://github.com/AdguardTeam/AdguardBrowserExtension/issues/481


    tab = await tabsImpl.get(tabId);
    onTabCreated(tab);
    return tab;
  };

  const isIncognito = function (tabId) {
    const tab = tabs[tabId];
    return tab && tab.incognito === true;
  }; // Records tab's frame


  const recordTabFrame = function (tabId, frameId, url, domainName) {
    let tab = tabs[tabId];

    if (!tab && frameId === 0) {
      // Sync tab for that 'onCreated' event was missed.
      // https://github.com/AdguardTeam/AdguardBrowserExtension/issues/481
      tab = {
        tabId,
        url,
        status: 'loading',
        // We mark this tabs as synthetic because actually they may not exists
        synthetic: true
      };
      onTabCreated(tab);
    }

    if (tab) {
      if (!tab.frames) {
        tab.frames = Object.create(null);
      }

      tab.frames[frameId] = {
        url,
        domainName
      };
    }
  };

  const clearTabFrames = function (tabId) {
    const tab = tabs[tabId];

    if (tab) {
      tab.frames = null;
    }
  }; // Gets tab's frame by id


  const getTabFrame = function (tabId, frameId) {
    const tab = tabs[tabId];

    if (tab && tab.frames) {
      return tab.frames[frameId || 0];
    }

    return null;
  };
  /**
   * Checks if the tab is new tab for popup or not
   * May be false positive for FF at least because new tab url in FF is "about:blank" too
   * @param tabId
   * @returns {boolean}
   */


  const isNewPopupTab = tabId => {
    const tab = tabs[tabId];

    if (!tab) {
      return false;
    }

    return !!(tab.url === '' || tab.url === 'about:blank');
  }; // Update tab metadata


  const updateTabMetadata = function (tabId, values) {
    const tab = tabs[tabId];

    if (tab) {
      if (!tab.metadata) {
        tab.metadata = Object.create(null);
      } // eslint-disable-next-line no-restricted-syntax


      for (const key in values) {
        if (values.hasOwnProperty && values.hasOwnProperty(key)) {
          tab.metadata[key] = values[key];
        }
      }
    }
  }; // Gets tab metadata


  const getTabMetadata = (tabId, key) => {
    const tab = tabs[tabId];

    if (tab && tab.metadata) {
      return tab.metadata[key];
    }

    return null;
  };

  const clearTabMetadata = tabId => {
    const tab = tabs[tabId];

    if (tab) {
      tab.metadata = null;
    }
  }; // Injecting resources to tabs


  const {
    insertCssCode
  } = tabsImpl;
  const {
    executeScriptCode
  } = tabsImpl;
  const {
    executeScriptFile
  } = tabsImpl;
  return {
    // Events
    onCreated: onCreatedChannel,
    onRemoved: onRemovedChannel,
    onUpdated: onUpdatedChannel,
    onActivated: onActivatedChannel,
    // Actions
    create,
    remove,
    activate,
    reload,
    sendMessage,
    getAll,
    forEach,
    getActive,
    isIncognito,
    updateUrl,
    // Frames
    recordTabFrame,
    clearTabFrames,
    getTabFrame,
    isNewPopupTab,
    // Other
    updateTabMetadata,
    getTabMetadata,
    clearTabMetadata,
    insertCssCode,
    executeScriptCode,
    executeScriptFile
  };
})(_extension_api_tabs__WEBPACK_IMPORTED_MODULE_1__/* .tabsImpl */ .W);



/***/ }),

/***/ 1654:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "z": () => (/* binding */ browserUtils)
/* harmony export */ });
/* harmony import */ var _prefs__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(4847);
/* harmony import */ var _storage__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(7789);
/* harmony import */ var _collections__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(4118);
/* harmony import */ var _tabs_tabs_api__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(6458);
/* harmony import */ var _extension_api_background_page__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(736);
/* harmony import */ var _extension_api_browser__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(2273);
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */






const browserUtils = function () {
  /**
   * Extension version (x.x.x)
   * @param version
   * @constructor
   */
  const Version = function (version) {
    this.version = Object.create(null);
    const parts = String(version || '').split('.');

    function parseVersionPart(part) {
      if (Number.isNaN(part)) {
        return 0;
      }

      return Math.max(part - 0, 0);
    }

    for (let i = 3; i >= 0; i -= 1) {
      this.version[i] = parseVersionPart(parts[i]);
    }
  };
  /**
   * Compares with other version
   * @param o
   * @returns {number}
   */


  Version.prototype.compare = function (o) {
    for (let i = 0; i < 4; i += 1) {
      if (this.version[i] > o.version[i]) {
        return 1;
      }

      if (this.version[i] < o.version[i]) {
        return -1;
      }
    }

    return 0;
  };

  const browserUtils = {
    getClientId() {
      let clientId = _storage__WEBPACK_IMPORTED_MODULE_1__/* .localStorage.getItem */ .X.getItem('client-id');

      if (!clientId) {
        const result = [];
        const suffix = Date.now() % 1e8;
        const symbols = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890';

        for (let i = 0; i < 8; i += 1) {
          const symbol = symbols[Math.floor(Math.random() * symbols.length)];
          result.push(symbol);
        }

        clientId = result.join('') + suffix;
        _storage__WEBPACK_IMPORTED_MODULE_1__/* .localStorage.setItem */ .X.setItem('client-id', clientId);
      }

      return clientId;
    },

    /**
     * Checks if version matches simple (without labels) semantic versioning scheme
     * https://semver.org/
     * @param {string} version
     * @return {boolean}
     */
    isSemver(version) {
      const semverRegex = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$/;
      return semverRegex.test(version);
    },

    /**
     * Checks if left version is greater than the right version
     */
    isGreaterVersion(leftVersion, rightVersion) {
      const left = new Version(leftVersion);
      const right = new Version(rightVersion);
      return left.compare(right) > 0;
    },

    isGreaterOrEqualsVersion(leftVersion, rightVersion) {
      const left = new Version(leftVersion);
      const right = new Version(rightVersion);
      return left.compare(right) >= 0;
    },

    /**
     * Returns major number of version
     *
     * @param version
     */
    getMajorVersionNumber(version) {
      const v = new Version(version);
      return v.version[0];
    },

    /**
     * Returns minor number of version
     *
     * @param version
     */
    getMinorVersionNumber(version) {
      const v = new Version(version);
      return v.version[1];
    },

    /**
     * @returns Extension version
     */
    getAppVersion() {
      return _storage__WEBPACK_IMPORTED_MODULE_1__/* .localStorage.getItem */ .X.getItem('app-version');
    },

    setAppVersion(version) {
      _storage__WEBPACK_IMPORTED_MODULE_1__/* .localStorage.setItem */ .X.setItem('app-version', version);
    },

    isYaBrowser() {
      return _prefs__WEBPACK_IMPORTED_MODULE_0__/* .prefs.browser */ .D.browser === 'YaBrowser';
    },

    isOperaBrowser() {
      return _prefs__WEBPACK_IMPORTED_MODULE_0__/* .prefs.browser */ .D.browser === 'Opera';
    },

    isEdgeBrowser() {
      return _prefs__WEBPACK_IMPORTED_MODULE_0__/* .prefs.browser */ .D.browser === 'Edge';
    },

    isEdgeChromiumBrowser() {
      return _prefs__WEBPACK_IMPORTED_MODULE_0__/* .prefs.browser */ .D.browser === 'EdgeChromium';
    },

    isFirefoxBrowser() {
      return _prefs__WEBPACK_IMPORTED_MODULE_0__/* .prefs.browser */ .D.browser === 'Firefox';
    },

    isChromeBrowser() {
      return _prefs__WEBPACK_IMPORTED_MODULE_0__/* .prefs.browser */ .D.browser === 'Chrome';
    },

    isChromium() {
      return _prefs__WEBPACK_IMPORTED_MODULE_0__/* .prefs.platform */ .D.platform === 'chromium';
    },

    isWindowsOs() {
      return navigator.userAgent.toLowerCase().indexOf('win') >= 0;
    },

    isMacOs() {
      return navigator.platform.toUpperCase().indexOf('MAC') >= 0;
    },

    getBrowser() {
      return _prefs__WEBPACK_IMPORTED_MODULE_0__/* .prefs.browser */ .D.browser;
    },

    getPlatform() {
      return _prefs__WEBPACK_IMPORTED_MODULE_0__/* .prefs.platform */ .D.platform;
    },

    /**
     * Finds header object by header name (case insensitive)
     * @param headers Headers collection
     * @param headerName Header name
     * @returns {*}
     */
    findHeaderByName(headers, headerName) {
      if (headers) {
        for (let i = 0; i < headers.length; i += 1) {
          const header = headers[i];

          if (header.name.toLowerCase() === headerName.toLowerCase()) {
            return header;
          }
        }
      }

      return null;
    },

    /**
     * Finds header value by name (case insensitive)
     * @param headers Headers collection
     * @param headerName Header name
     * @returns {null}
     */
    getHeaderValueByName(headers, headerName) {
      const header = this.findHeaderByName(headers, headerName);
      return header ? header.value : null;
    },

    /**
     * Set header value. Only for Chrome
     * @param headers
     * @param headerName
     * @param headerValue
     */
    setHeaderValue(headers, headerName, headerValue) {
      if (!headers) {
        headers = [];
      }

      const header = this.findHeaderByName(headers, headerName);

      if (header) {
        header.value = headerValue;
      } else {
        headers.push({
          name: headerName,
          value: headerValue
        });
      }

      return headers;
    },

    /**
     * Removes header from headers by name
     *
     * @param {Array} headers
     * @param {String} headerName
     * @return {boolean} True if header were removed
     */
    removeHeader(headers, headerName) {
      let removed = false;

      if (headers) {
        for (let i = headers.length - 1; i >= 0; i -= 1) {
          const header = headers[i];

          if (header.name.toLowerCase() === headerName.toLowerCase()) {
            headers.splice(i, 1);
            removed = true;
          }
        }
      }

      return removed;
    },

    getSafebrowsingBackUrl(tab) {
      // https://code.google.com/p/chromium/issues/detail?id=11854
      const previousUrl = _tabs_tabs_api__WEBPACK_IMPORTED_MODULE_3__/* .tabsApi.getTabMetadata */ .n.getTabMetadata(tab.tabId, 'previousUrl');

      if (previousUrl && previousUrl.indexOf('http') === 0) {
        return previousUrl;
      }

      const referrerUrl = _tabs_tabs_api__WEBPACK_IMPORTED_MODULE_3__/* .tabsApi.getTabMetadata */ .n.getTabMetadata(tab.tabId, 'referrerUrl');

      if (referrerUrl && referrerUrl.indexOf('http') === 0) {
        return referrerUrl;
      }

      return 'about:newtab';
    },

    /**
     * Retrieve languages from navigator
     * @param {number} [limit] Limit of preferred languages
     * @returns {Array}
     */
    getNavigatorLanguages(limit) {
      let languages = []; // https://developer.mozilla.org/ru/docs/Web/API/NavigatorLanguage/languages

      if (_collections__WEBPACK_IMPORTED_MODULE_2__/* .collections.isArray */ .s.isArray(navigator.languages)) {
        // get all languages if 'limit' is not specified
        const langLimit = limit || navigator.languages.length;
        languages = navigator.languages.slice(0, langLimit);
      } else if (navigator.language) {
        languages.push(navigator.language); // .language is first in .languages
      }

      return languages;
    },

    /**
     * Affected issues:
     * https://github.com/AdguardTeam/AdguardBrowserExtension/issues/602
     * https://github.com/AdguardTeam/AdguardBrowserExtension/issues/566
     * 'Popup' window
     * Creators update is not yet released, so we use Insider build 15063 instead.
     */
    EDGE_CREATORS_UPDATE: 15063,

    isEdgeBeforeCreatorsUpdate() {
      return this.isEdgeBrowser() && _prefs__WEBPACK_IMPORTED_MODULE_0__/* .prefs.edgeVersion.build */ .D.edgeVersion.build < this.EDGE_CREATORS_UPDATE;
    },

    /**
     * Returns extension params: clientId, version and locale
     */
    getExtensionParams() {
      const clientId = encodeURIComponent(this.getClientId());
      const locale = encodeURIComponent(_extension_api_background_page__WEBPACK_IMPORTED_MODULE_4__/* .backgroundPage.app.getLocale */ .$.app.getLocale());
      const version = encodeURIComponent(_extension_api_background_page__WEBPACK_IMPORTED_MODULE_4__/* .backgroundPage.app.getVersion */ .$.app.getVersion());
      const id = encodeURIComponent(_extension_api_background_page__WEBPACK_IMPORTED_MODULE_4__/* .backgroundPage.app.getId */ .$.app.getId());
      const params = [];
      params.push(`v=${version}`);
      params.push(`cid=${clientId}`);
      params.push(`lang=${locale}`);
      params.push(`id=${id}`);
      return params;
    },

    /**
     * @typedef PermissionsObj
     * A Permissions object represents a collection of permissions
     * https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/API/permissions/Permissions
     * @property {Array<string>} permissions
     * @property {Array<string>} [origins]
     */

    /**
     * Checks if extension has required permissions
     * @param {PermissionsObj} permissions
     * @returns {Promise<boolean>}
     */
    containsPermissions: permissions => {
      return _extension_api_browser__WEBPACK_IMPORTED_MODULE_5__/* .browser.permissions.contains */ .X.permissions.contains(permissions);
    },

    /**
     * Requests required permissions
     * @param {PermissionsObj} permissions
     * @returns {Promise<boolean>}
     */
    requestPermissions: permissions => {
      return _extension_api_browser__WEBPACK_IMPORTED_MODULE_5__/* .browser.permissions.request */ .X.permissions.request(permissions);
    },

    /**
     * Removes required permissions
     * @param {PermissionsObj} permissions
     * @returns {Promise<boolean>}
     */
    removePermission: permissions => {
      return _extension_api_browser__WEBPACK_IMPORTED_MODULE_5__/* .browser.permissions.remove */ .X.permissions.remove(permissions);
    }
  };
  return browserUtils;
}();

/***/ }),

/***/ 8098:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Z": () => (/* binding */ channels)
/* harmony export */ });
/* eslint-disable prefer-rest-params */

/**
 * Simple publish-subscribe implementation
 */
const channels = (() => {
  const EventChannels = (() => {
    const EventChannel = function () {
      let listeners = null;
      let listenerCallback = null;

      const addListener = function (callback) {
        if (typeof callback !== 'function') {
          throw new Error('Illegal callback');
        }

        if (listeners !== null) {
          listeners.push(callback);
          return;
        }

        if (listenerCallback !== null) {
          listeners = [];
          listeners.push(listenerCallback);
          listeners.push(callback);
          listenerCallback = null;
        } else {
          listenerCallback = callback;
        }
      };

      const removeListener = function (callback) {
        if (listenerCallback !== null) {
          listenerCallback = null;
        } else {
          const index = listeners.indexOf(callback);

          if (index >= 0) {
            listeners.splice(index, 1);
          }
        }
      };

      const notify = function () {
        if (listenerCallback !== null) {
          return listenerCallback.apply(listenerCallback, arguments);
        }

        if (listeners !== null) {
          for (let i = 0; i < listeners.length; i += 1) {
            const listener = listeners[i];
            listener.apply(listener, arguments);
          }
        }
      };

      const notifyInReverseOrder = function () {
        if (listenerCallback !== null) {
          return listenerCallback.apply(listenerCallback, arguments);
        }

        if (listeners !== null) {
          for (let i = listeners.length - 1; i >= 0; i -= 1) {
            const listener = listeners[i];
            listener.apply(listener, arguments);
          }
        }
      };

      return {
        addListener,
        removeListener,
        notify,
        notifyInReverseOrder
      };
    };

    const namedChannels = Object.create(null);

    const newChannel = function () {
      return new EventChannel();
    };

    const newNamedChannel = function (name) {
      const channel = newChannel();
      namedChannels[name] = channel;
      return channel;
    };

    const getNamedChannel = function (name) {
      return namedChannels[name];
    };

    return {
      newChannel,
      newNamedChannel,
      getNamedChannel
    };
  })();

  return EventChannels;
})();

/***/ }),

/***/ 4118:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "s": () => (/* binding */ collections)
/* harmony export */ });
/**
 * Util class for work with collections
 */
const collections = (() => {
  const CollectionUtils = {
    remove(collection, element) {
      if (!element || !collection) {
        return;
      }

      const index = collection.indexOf(element);

      if (index >= 0) {
        collection.splice(index, 1);
      }
    },

    removeAll(collection, element) {
      if (!element || !collection) {
        return;
      }

      for (let i = collection.length - 1; i >= 0; i -= 1) {
        if (collection[i] === element) {
          collection.splice(i, 1);
        }
      }
    },

    /**
     * Removes elements from collection if predicate returns true
     * @param collection
     * @param predicate
     */
    removeBy(collection, predicate) {
      if (!predicate || !collection) {
        return;
      }

      for (let i = collection.length - 1; i >= 0; i -= 1) {
        if (predicate(collection[i])) {
          collection.splice(i, 1);
        }
      }
    },

    removeRule(collection, rule) {
      if (!rule || !collection) {
        return;
      }

      for (let i = collection.length - 1; i >= 0; i -= 1) {
        if (rule.getText() === collection[i].getText()) {
          collection.splice(i, 1);
        }
      }
    },

    removeDuplicates(arr) {
      if (!arr || arr.length === 1) {
        return arr;
      }

      return arr.filter((elem, pos) => arr.indexOf(elem) === pos);
    },

    getRulesText(collection) {
      const text = [];

      if (!collection) {
        return text;
      }

      for (let i = 0; i < collection.length; i += 1) {
        text.push(collection[i].getText());
      }

      return text;
    },

    /**
     * Find element in array by property
     * @param array
     * @param property
     * @param value
     * @returns {*}
     */
    find(array, property, value) {
      if (typeof array.find === 'function') {
        return array.find(a => a[property] === value);
      }

      for (let i = 0; i < array.length; i += 1) {
        const elem = array[i];

        if (elem[property] === value) {
          return elem;
        }
      }

      return null;
    },

    /**
     * Checks if specified object is array
     * We don't use instanceof because it is too slow: http://jsperf.com/instanceof-performance/2
     * @param obj Object
     */
    isArray: Array.isArray || function (obj) {
      return `${obj}` === '[object Array]';
    },

    /**
     * Returns array elements of a, which is not included in b
     *
     * @param a
     * @param b
     */
    getArraySubtraction(a, b) {
      return a.filter(i => b.indexOf(i) < 0);
    }

  };
  return CollectionUtils;
})();

/***/ }),

/***/ 5088:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "HB": () => (/* binding */ BACKGROUND_TAB_ID),
/* harmony export */   "P6": () => (/* binding */ utils),
/* harmony export */   "bw": () => (/* binding */ toTabFromChromeTab)
/* harmony export */ });
/* unused harmony exports MAIN_FRAME_ID, unload */
/* harmony import */ var _common_log__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(9224);
/* harmony import */ var _common_strings__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(9100);
/* harmony import */ var _dates__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(1572);
/* harmony import */ var _collections__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(4118);
/* harmony import */ var _concurrent__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(4225);
/* harmony import */ var _channels__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(8098);
/* harmony import */ var _workaround__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(284);
/* harmony import */ var _i18n__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(2721);
/* harmony import */ var _filters__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(9272);
/* harmony import */ var _url__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(2818);
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */










/**
 * Background tab id in browsers is defined as -1
 */

const BACKGROUND_TAB_ID = -1;
/**
 * Main frame id is equal to 0
 */

const MAIN_FRAME_ID = 0;
/**
 * Utilities namespace
 */

const utils = {
  strings: _common_strings__WEBPACK_IMPORTED_MODULE_1__/* .strings */ .j,
  dates: _dates__WEBPACK_IMPORTED_MODULE_2__/* .dates */ .p,
  collections: _collections__WEBPACK_IMPORTED_MODULE_3__/* .collections */ .s,
  concurrent: _concurrent__WEBPACK_IMPORTED_MODULE_4__/* .concurrent */ .d,
  channels: _channels__WEBPACK_IMPORTED_MODULE_5__/* .channels */ .Z,
  workaround: _workaround__WEBPACK_IMPORTED_MODULE_6__/* .workaround */ .c,
  i18n: _i18n__WEBPACK_IMPORTED_MODULE_7__/* .i18n */ .a,
  filters: _filters__WEBPACK_IMPORTED_MODULE_8__/* .filters */ .u,
  url: _url__WEBPACK_IMPORTED_MODULE_9__/* .url */ .H
};
/**
 * Converts chrome tabs into tabs
 * https://developer.chrome.com/extensions/tabs#type-Tab
 * @param chromeTab
 * @returns tab
 */

function toTabFromChromeTab(chromeTab) {
  return {
    tabId: chromeTab.id,
    url: chromeTab.url,
    title: chromeTab.title,
    incognito: chromeTab.incognito,
    status: chromeTab.status
  };
}
/**
 * Unload handler. When extension is unload then 'fireUnload' is invoked.
 * You can add own handler with method 'when'
 * @type {{when, fireUnload}}
 */

const unload = function () {
  const unloadChannel = utils.channels.newChannel();

  const when = function (callback) {
    if (typeof callback !== 'function') {
      return;
    }

    unloadChannel.addListener(() => {
      try {
        callback();
      } catch (ex) {
        _common_log__WEBPACK_IMPORTED_MODULE_0__/* .log.error */ .c.error('Error while invoke unload method');
        _common_log__WEBPACK_IMPORTED_MODULE_0__/* .log.error */ .c.error(ex);
      }
    });
  };

  const fireUnload = function (reason) {
    _common_log__WEBPACK_IMPORTED_MODULE_0__/* .log.info */ .c.info(`Unload is fired: ${reason}`);
    unloadChannel.notifyInReverseOrder(reason);
  };

  return {
    when,
    fireUnload
  };
}();

/***/ }),

/***/ 4225:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "d": () => (/* binding */ concurrent)
/* harmony export */ });
/* eslint-disable prefer-rest-params */

/**
 * Util class for support timeout, retry operations, debounce
 */
const concurrent = function () {
  const ConcurrentUtils = {
    runAsync(callback, context) {
      const params = Array.prototype.slice.call(arguments, 2);
      setTimeout(() => {
        callback.apply(context, params);
      }, 0);
    },

    retryUntil(predicate, main, details) {
      if (typeof details !== 'object') {
        details = {};
      }

      let now = 0;
      const next = details.next || 200;
      const until = details.until || 2000;

      const check = function () {
        if (predicate() === true || now >= until) {
          main();
          return;
        }

        now += next;
        setTimeout(check, next);
      };

      setTimeout(check, 1);
    },

    debounce(func, wait) {
      let timeout;
      return function () {
        const context = this;
        const args = arguments;

        const later = function () {
          timeout = null;
          func.apply(context, args);
        };

        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
      };
    },

    /**
     * Returns a new function that, when invoked, invokes `func` at most once per `wait` milliseconds.
     * https://github.com/component/throttle
     *
     * @param {Function} func Function to wrap.
     * @param {Number} wait Number of milliseconds that must elapse between `func` invocations.
     * @return {Function} A new function that wraps the `func` function passed in.
     */
    throttle(func, wait) {
      let ctx;
      let args;
      let rtn;
      let timeoutID; // caching

      let last = 0;

      function call() {
        timeoutID = 0;
        last = +new Date();
        rtn = func.apply(ctx, args);
        ctx = null;
        args = null;
      }

      return function throttled() {
        ctx = this;
        args = arguments;
        const delta = new Date() - last;

        if (!timeoutID) {
          if (delta >= wait) {
            call();
          } else {
            timeoutID = setTimeout(call, wait - delta);
          }
        }

        return rtn;
      };
    }

  };
  return ConcurrentUtils;
}();

/***/ }),

/***/ 1572:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "p": () => (/* binding */ dates)
/* harmony export */ });
/**
 * Util class for dates
 */
const dates = function () {
  const DateUtils = {
    isSameHour(a, b) {
      return this.isSameDay(a, b) && a.getHours() === b.getHours();
    },

    isSameDay(a, b) {
      return this.isSameMonth(a, b) && a.getDate() === b.getDate();
    },

    isSameMonth(a, b) {
      if (!a || !b) {
        return false;
      }

      return a.getYear() === b.getYear() && a.getMonth() === b.getMonth();
    },

    getDifferenceInHours(a, b) {
      return (a.getTime() - b.getTime()) / 1000 / 60 / 60;
    },

    getDifferenceInDays(a, b) {
      return this.getDifferenceInHours(a, b) / 24;
    },

    getDifferenceInMonths(a, b) {
      return this.getDifferenceInDays(a, b) / 30;
    }

  };
  return DateUtils;
}();

/***/ }),

/***/ 9272:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "u": () => (/* binding */ filters)
/* harmony export */ });
/* harmony import */ var _common_constants__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(4568);

/**
 * Util class for detect filter type. Includes various filter identifiers
 */

const filters = (() => {
  const FilterUtils = {
    isUserFilterRule(rule) {
      return rule.getFilterListId() === _common_constants__WEBPACK_IMPORTED_MODULE_0__/* .ANTIBANNER_FILTERS_ID.USER_FILTER_ID */ .gu.USER_FILTER_ID;
    },

    isAllowlistFilterRule(rule) {
      return rule.getFilterListId() === _common_constants__WEBPACK_IMPORTED_MODULE_0__/* .ANTIBANNER_FILTERS_ID.ALLOWLIST_FILTER_ID */ .gu.ALLOWLIST_FILTER_ID;
    }

  }; // Make accessible only constants without functions. They will be passed to content-page

  FilterUtils.ids = _common_constants__WEBPACK_IMPORTED_MODULE_0__/* .ANTIBANNER_FILTERS_ID */ .gu; // Copy filter ids to api

  Object.keys(_common_constants__WEBPACK_IMPORTED_MODULE_0__/* .ANTIBANNER_FILTERS_ID */ .gu).forEach(key => {
    FilterUtils[key] = _common_constants__WEBPACK_IMPORTED_MODULE_0__/* .ANTIBANNER_FILTERS_ID */ .gu[key];
  });
  return FilterUtils;
})();

/***/ }),

/***/ 2721:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "a": () => (/* binding */ i18n)
/* harmony export */ });
/* harmony import */ var _collections__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(4118);

/**
 * Simple i18n utils
 */

const i18n = function () {
  function isArrayElement(array, elem) {
    return array.indexOf(elem) >= 0;
  }

  function isObjectKey(object, key) {
    return key in object;
  }

  return {
    /**
     * Tries to find locale in the given collection of locales
     * @param locales Collection of locales (array or object)
     * @param locale Locale (e.g. en, en_GB, pt_BR)
     * @returns matched locale from the locales collection or null
     */
    normalize(locales, locale) {
      if (!locale) {
        return null;
      } // Transform Language-Country => Language_Country


      locale = locale.replace('-', '_');
      let search;

      if (_collections__WEBPACK_IMPORTED_MODULE_0__/* .collections.isArray */ .s.isArray(locales)) {
        search = isArrayElement;
      } else {
        search = isObjectKey;
      }

      if (search(locales, locale)) {
        return locale;
      } // Try to search by the language


      const parts = locale.split('_');
      const language = parts[0];

      if (search(locales, language)) {
        return language;
      }

      return null;
    }

  };
}();

/***/ }),

/***/ 1255:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "$": () => (/* binding */ lazyGet)
/* harmony export */ });
/* unused harmony export lazyGetClear */
/**
 * This function allows cache property in object. Use with javascript getter.
 *
 * var Object = {
 *
 *      get someProperty(){
 *          return lazyGet(Object, 'someProperty', function() {
 *              return calculateSomeProperty();
 *          });
 *      }
 * }
 *
 * @param object Object
 * @param prop Original property name
 * @param calculateFunc Calculation function
 * @returns {*}
 */
const lazyGet = function (object, prop, calculateFunc) {
  const cachedProp = `_${prop}`;

  if (cachedProp in object) {
    return object[cachedProp];
  }

  const value = calculateFunc.apply(object);
  object[cachedProp] = value;
  return value;
};
/**
 * Clear cached property
 * @param object Object
 * @param prop Original property name
 */

const lazyGetClear = function (object, prop) {
  delete object[`_${prop}`];
};

/***/ }),

/***/ 5131:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "i": () => (/* binding */ localStorageImpl)
/* harmony export */ });
/* harmony import */ var _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(2273);
/* harmony import */ var _common_log__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(9224);
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */


/**
 * Local storage implementation for chromium-based browsers
 */

const localStorageImpl = function () {
  const ADGUARD_SETTINGS_PROP = 'adguard-settings';
  let values = null;
  /**
   * Reads data from storage.local
   * @param path Path
   */

  async function read(path) {
    const results = await _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.storage.local.get */ .X.storage.local.get(path);
    return results ? results[path] : null;
  }
  /**
   * Writes data to storage.local
   * @param path Path
   * @param data Data to write
   */


  async function write(path, data) {
    const item = {};
    item[path] = data;
    await _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.storage.local.set */ .X.storage.local.set(item);
  }
  /**
   * Due to async initialization of storage, we have to check it before accessing values object
   * @returns {boolean}
   */


  function isInitialized() {
    return values !== null;
  }
  /**
   * Retrieves value by key from cached values
   * @param key
   * @returns {*}
   */


  function getItem(key) {
    if (!isInitialized()) {
      return null;
    }

    return values[key];
  }

  function setItem(key, value) {
    if (!isInitialized()) {
      return;
    }

    values[key] = value;
    write(ADGUARD_SETTINGS_PROP, values);
  }

  function removeItem(key) {
    if (!isInitialized()) {
      return;
    }

    delete values[key];
    write(ADGUARD_SETTINGS_PROP, values);
  }

  function hasItem(key) {
    if (!isInitialized()) {
      return false;
    }

    return key in values;
  }
  /**
   * We can't use localStorage object anymore and we've decided to store all data into storage.local
   * localStorage is affected by cleaning tools: https://github.com/AdguardTeam/AdguardBrowserExtension/issues/681
   * storage.local has async nature and we have to preload all key-values pairs into memory on extension startup
   */


  async function init() {
    if (isInitialized()) {
      // Already initialized
      return;
    }

    let items;

    try {
      items = await read(ADGUARD_SETTINGS_PROP);
    } catch (e) {
      _common_log__WEBPACK_IMPORTED_MODULE_1__/* .log.error */ .c.error(e);
    }

    values = items || Object.create(null);
  }

  return {
    getItem,
    setItem,
    removeItem,
    hasItem,
    init,
    isInitialized
  };
}();

/***/ }),

/***/ 5753:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "R": () => (/* binding */ hasAllOptionalPermissions),
/* harmony export */   "f": () => (/* binding */ requestOptionalPermissions)
/* harmony export */ });
/* harmony import */ var _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(2273);
/* harmony import */ var _browser_utils__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(1654);



const getPermissionsToRequest = () => {
  const permissions = {
    origins: [],
    permissions: []
  };
  const {
    optional_permissions: optionalPermissions
  } = _extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.runtime.getManifest */ .X.runtime.getManifest();

  if (optionalPermissions.includes('<all_urls>')) {
    permissions.origins.push('<all_urls>');
    optionalPermissions.splice(optionalPermissions.indexOf('<all_urls>'), 1);
  }

  permissions.permissions = [...optionalPermissions];
  return permissions;
};

const hasAllOptionalPermissions = async () => {
  const permissions = getPermissionsToRequest();
  return _browser_utils__WEBPACK_IMPORTED_MODULE_1__/* .browserUtils.containsPermissions */ .z.containsPermissions(permissions);
};
const requestOptionalPermissions = async () => {
  const permissions = getPermissionsToRequest();
  return _browser_utils__WEBPACK_IMPORTED_MODULE_1__/* .browserUtils.requestPermissions */ .z.requestPermissions(permissions);
};

/***/ }),

/***/ 3485:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "O": () => (/* binding */ parseContentTypeFromUrlPath),
/* harmony export */   "l": () => (/* binding */ RequestTypes)
/* harmony export */ });
/* harmony import */ var _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(8261);

/**
 * Request types enumeration
 */

const RequestTypes = {
  /**
   * Document that is loaded for a top-level frame
   */
  DOCUMENT: 'DOCUMENT',

  /**
   * Document that is loaded for an embedded frame (iframe)
   */
  SUBDOCUMENT: 'SUBDOCUMENT',
  SCRIPT: 'SCRIPT',
  STYLESHEET: 'STYLESHEET',
  OBJECT: 'OBJECT',
  IMAGE: 'IMAGE',
  XMLHTTPREQUEST: 'XMLHTTPREQUEST',
  MEDIA: 'MEDIA',
  FONT: 'FONT',
  WEBSOCKET: 'WEBSOCKET',
  WEBRTC: 'WEBRTC',
  OTHER: 'OTHER',
  CSP: 'CSP',
  COOKIE: 'COOKIE',
  PING: 'PING',
  CSP_REPORT: 'CSP_REPORT',

  /**
   * Transforms to TSUrlFilter.RequestType
   *
   * @param requestType
   * @return {number}
   */
  transformRequestType(requestType) {
    const contentTypes = RequestTypes;

    switch (requestType) {
      case contentTypes.DOCUMENT:
        return _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Document */ .x.Document;

      case contentTypes.SUBDOCUMENT:
        return _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Subdocument */ .x.Subdocument;

      case contentTypes.STYLESHEET:
        return _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Stylesheet */ .x.Stylesheet;

      case contentTypes.FONT:
        return _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Font */ .x.Font;

      case contentTypes.IMAGE:
        return _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Image */ .x.Image;

      case contentTypes.MEDIA:
        return _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Media */ .x.Media;

      case contentTypes.SCRIPT:
        return _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Script */ .x.Script;

      case contentTypes.XMLHTTPREQUEST:
        return _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.XmlHttpRequest */ .x.XmlHttpRequest;

      case contentTypes.WEBSOCKET:
        return _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Websocket */ .x.Websocket;

      case contentTypes.WEBRTC:
        return _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Webrtc */ .x.Webrtc;

      case contentTypes.PING:
        return _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Ping */ .x.Ping;

      default:
        return _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Other */ .x.Other;
    }
  },

  /**
   * Transforms from TSUrlFilter.RequestType
   *
   * @param requestType
   * @return {string}
   */
  transformRequestTypeFromTs(requestType) {
    const contentTypes = RequestTypes;

    switch (requestType) {
      case _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Document */ .x.Document:
        return contentTypes.DOCUMENT;

      case _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Subdocument */ .x.Subdocument:
        return contentTypes.SUBDOCUMENT;

      case _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Stylesheet */ .x.Stylesheet:
        return contentTypes.STYLESHEET;

      case _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Font */ .x.Font:
        return contentTypes.FONT;

      case _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Image */ .x.Image:
        return contentTypes.IMAGE;

      case _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Media */ .x.Media:
        return contentTypes.MEDIA;

      case _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Script */ .x.Script:
        return contentTypes.SCRIPT;

      case _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.XmlHttpRequest */ .x.XmlHttpRequest:
        return contentTypes.XMLHTTPREQUEST;

      case _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Websocket */ .x.Websocket:
        return contentTypes.WEBSOCKET;

      case _adguard_tsurlfilter_dist_es_request_type__WEBPACK_IMPORTED_MODULE_0__/* .RequestType.Ping */ .x.Ping:
        return contentTypes.PING;

      default:
        return contentTypes.OTHER;
    }
  }

};
/**
 * Parse content type from path
 * @param path Path
 * @returns {*} content type (RequestTypes.*) or null
 */

function parseContentTypeFromUrlPath(path) {
  const objectContentTypes = '.jar.swf.';
  const mediaContentTypes = '.mp4.flv.avi.m3u.webm.mpeg.3gp.3gpp.3g2.3gpp2.ogg.mov.qt.';
  const fontContentTypes = '.ttf.otf.woff.woff2.eot.';
  const imageContentTypes = '.ico.png.gif.jpg.jpeg.webp.';
  let ext = path.slice(-6);
  const pos = ext.lastIndexOf('.'); // Unable to parse extension from url

  if (pos === -1) {
    return null;
  }

  ext = `${ext.slice(pos)}.`;

  if (objectContentTypes.indexOf(ext) !== -1) {
    return RequestTypes.OBJECT;
  }

  if (mediaContentTypes.indexOf(ext) !== -1) {
    return RequestTypes.MEDIA;
  }

  if (fontContentTypes.indexOf(ext) !== -1) {
    return RequestTypes.FONT;
  }

  if (imageContentTypes.indexOf(ext) !== -1) {
    return RequestTypes.IMAGE;
  }

  return null;
}

/***/ }),

/***/ 2818:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "H": () => (/* binding */ url)
/* harmony export */ });
/* harmony import */ var punycode__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(2860);
/* harmony import */ var _common_strings__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(9100);
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */

/* eslint-disable camelcase, no-control-regex, max-len */


const url = function () {
  const RE_V4 = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|0x[0-9a-f][0-9a-f]?|0[0-7]{3})\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|0x[0-9a-f][0-9a-f]?|0[0-7]{3})$/i;
  const RE_V4_HEX = /^0x([0-9a-f]{8})$/i;
  const RE_V4_NUMERIC = /^[0-9]+$/;
  const RE_V4inV6 = /(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
  const RE_BAD_CHARACTERS = /([^0-9a-f:])/i;
  const RE_BAD_ADDRESS = /([0-9a-f]{5,}|:{3,}|[^:]:$|^:[^:]$)/i;
  /**
   * Helper methods to work with URLs
   */

  const UrlUtils = {
    isHttpRequest(url) {
      return url && url.indexOf('http') === 0;
    },

    isHttpOrWsRequest(url) {
      return url && (url.indexOf('http') === 0 || url.indexOf('ws') === 0);
    },

    toPunyCode(domain) {
      if (!domain) {
        return '';
      }

      if (/^[\x00-\x7F]+$/.test(domain)) {
        return domain;
      }

      return punycode__WEBPACK_IMPORTED_MODULE_0__/* ["default"].toASCII */ .ZP.toASCII(domain);
    },

    /**
     * Retrieves hostname from URL
     */
    getHost(url) {
      if (!url) {
        return null;
      }

      let firstIdx = url.indexOf('//');

      if (firstIdx === -1) {
        /**
         * It's non hierarchical structured URL (e.g. stun: or turn:)
         * https://tools.ietf.org/html/rfc4395#section-2.2
         * https://tools.ietf.org/html/draft-nandakumar-rtcweb-stun-uri-08#appendix-B
         */
        firstIdx = url.indexOf(':');

        if (firstIdx === -1) {
          return null;
        }

        firstIdx -= 1;
      }

      const nextSlashIdx = url.indexOf('/', firstIdx + 2);
      const startParamsIdx = url.indexOf('?', firstIdx + 2);
      let lastIdx = nextSlashIdx;

      if (startParamsIdx > 0 && (startParamsIdx < nextSlashIdx || nextSlashIdx < 0)) {
        lastIdx = startParamsIdx;
      }

      let host = lastIdx === -1 ? url.substring(firstIdx + 2) : url.substring(firstIdx + 2, lastIdx);
      const portIndex = host.indexOf(':');
      host = portIndex === -1 ? host : host.substring(0, portIndex); // https://github.com/AdguardTeam/AdguardBrowserExtension/issues/1586

      const lastChar = host.charAt(host.length - 1);

      if (lastChar === '.') {
        host = host.slice(0, -1);
      }

      return host;
    },

    getDomainName(url) {
      const host = this.getHost(url);
      return this.getCroppedDomainName(host);
    },

    getCroppedDomainName(host) {
      return _common_strings__WEBPACK_IMPORTED_MODULE_1__/* .strings.startWith */ .j.startWith(host, 'www.') ? host.substring(4) : host;
    },

    isIpv4(address) {
      if (RE_V4.test(address)) {
        return true;
      }

      if (RE_V4_HEX.test(address)) {
        return true;
      }

      if (RE_V4_NUMERIC.test(address)) {
        return true;
      }

      return false;
    },

    isIpv6(address) {
      let a4addon = 0;
      const address4 = address.match(RE_V4inV6);

      if (address4) {
        const temp4 = address4[0].split('.');

        for (let i = 0; i < 4; i += 1) {
          if (/^0[0-9]+/.test(temp4[i])) {
            return false;
          }
        }

        address = address.replace(RE_V4inV6, '');

        if (/[0-9]$/.test(address)) {
          return false;
        }

        address += temp4.join(':');
        a4addon = 2;
      }

      if (RE_BAD_CHARACTERS.test(address)) {
        return false;
      }

      if (RE_BAD_ADDRESS.test(address)) {
        return false;
      }

      function count(string, substring) {
        return (string.length - string.replace(new RegExp(substring, 'g'), '').length) / substring.length;
      }

      const halves = count(address, '::');

      if (halves === 1 && count(address, ':') <= 6 + 2 + a4addon) {
        return true;
      }

      if (halves === 0 && count(address, ':') === 7 + a4addon) {
        return true;
      }

      return false;
    },

    urlEquals(u1, u2) {
      if (!u1 || !u2) {
        return false;
      } // eslint-disable-next-line prefer-destructuring


      u1 = u1.split(/[#?]/)[0]; // eslint-disable-next-line prefer-destructuring

      u2 = u2.split(/[#?]/)[0];
      return u1 === u2;
    }

  };
  return UrlUtils;
}();

/***/ }),

/***/ 284:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "c": () => (/* binding */ workaround)
/* harmony export */ });
/**
 * We collect here all workarounds and ugly hacks:)
 */
const workaround = function () {
  const WorkaroundUtils = {
    /**
     * Converts blocked counter to the badge text.
     * Workaround for FF - make 99 max.
     *
     * @param blocked Blocked requests count
     */
    getBlockedCountText(blocked) {
      let blockedText = blocked === '0' ? '' : blocked;

      if (blocked - 0 > 99) {
        blockedText = '\u221E';
      }

      return blockedText;
    }

  };
  return WorkaroundUtils;
}();

/***/ }),

/***/ 1351:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "V": () => (/* binding */ runtimeImpl)
/* harmony export */ });
/* unused harmony export i18n */
/* harmony import */ var _background_extension_api_browser__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(2273);
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */

const runtimeImpl = (() => {
  return {
    onMessage: _background_extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.runtime.onMessage */ .X.runtime.onMessage,
    sendMessage: _background_extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.runtime.sendMessage */ .X.runtime.sendMessage
  };
})(); // eslint-disable-next-line prefer-destructuring

const i18n = _background_extension_api_browser__WEBPACK_IMPORTED_MODULE_0__/* .browser.i18n */ .X.i18n;

/***/ }),

/***/ 9100:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "j": () => (/* binding */ strings)
/* harmony export */ });
/**
 * Util class for work with strings
 */
const strings = (() => {
  const StringUtils = {
    isEmpty(str) {
      return !str || str.trim().length === 0;
    },

    startWith(str, prefix) {
      return str && str.indexOf(prefix) === 0;
    },

    endsWith(str, postfix) {
      return str.endsWith(postfix);
    },

    substringAfter(str, separator) {
      if (!str) {
        return str;
      }

      const index = str.indexOf(separator);
      return index < 0 ? '' : str.substring(index + separator.length);
    },

    substringBefore(str, separator) {
      if (!str || !separator) {
        return str;
      }

      const index = str.indexOf(separator);
      return index < 0 ? str : str.substring(0, index);
    },

    contains(str, searchString) {
      return str && str.indexOf(searchString) >= 0;
    },

    containsIgnoreCase(str, searchString) {
      return str && searchString && str.toUpperCase().indexOf(searchString.toUpperCase()) >= 0;
    },

    replaceAll(str, find, replace) {
      if (!str) {
        return str;
      }

      return str.split(find).join(replace);
    },

    join(array, separator, startIndex, endIndex) {
      if (!array) {
        return null;
      }

      if (!startIndex) {
        startIndex = 0;
      }

      if (!endIndex) {
        endIndex = array.length;
      }

      if (startIndex >= endIndex) {
        return '';
      }

      const buf = [];

      for (let i = startIndex; i < endIndex; i += 1) {
        buf.push(array[i]);
      }

      return buf.join(separator);
    },

    /**
     * Get string before regexp first match
     * @param {string} str
     * @param {RegExp} rx
     */
    getBeforeRegExp(str, rx) {
      const index = str.search(rx);
      return str.substring(0, index);
    },

    /**
     * Look for any symbol from "chars" array starting at "start" index or from the start of the string
     *
     * @param str   String to search
     * @param chars Chars to search for
     * @param start Start index (optional, inclusive)
     * @return int Index of the element found or null
     */
    indexOfAny(str, chars, start) {
      start = start || 0;

      if (typeof str === 'string' && str.length <= start) {
        return -1;
      }

      for (let i = start; i < str.length; i += 1) {
        const c = str.charAt(i);

        if (chars.indexOf(c) > -1) {
          return i;
        }
      }

      return -1;
    },

    /**
     * Splits string by a delimiter, ignoring escaped delimiters
     * @param str               String to split
     * @param delimiter         Delimiter
     * @param escapeCharacter   Escape character
     * @param preserveAllTokens If true - preserve empty entries.
     */
    splitByDelimiterWithEscapeCharacter(str, delimiter, escapeCharacter, preserveAllTokens) {
      const parts = [];

      if (this.isEmpty(str)) {
        return parts;
      }

      let sb = [];

      for (let i = 0; i < str.length; i += 1) {
        const c = str.charAt(i);

        if (c === delimiter) {
          if (i === 0) {// Ignore
          } else if (str.charAt(i - 1) === escapeCharacter) {
            sb.splice(sb.length - 1, 1);
            sb.push(c);
          } else if (preserveAllTokens || sb.length > 0) {
            const part = sb.join('');
            parts.push(part);
            sb = [];
          }
        } else {
          sb.push(c);
        }
      }

      if (preserveAllTokens || sb.length > 0) {
        parts.push(sb.join(''));
      }

      return parts;
    },

    /**
     * Serialize HTML element
     * @param element
     */
    elementToString(element) {
      const s = [];
      s.push('<');
      s.push(element.localName);
      const {
        attributes
      } = element;

      for (let i = 0; i < attributes.length; i += 1) {
        const attr = attributes[i];
        s.push(' ');
        s.push(attr.name);
        s.push('="');
        const value = attr.value === null ? '' : attr.value.replace(/"/g, '\\"');
        s.push(value);
        s.push('"');
      }

      s.push('>');
      return s.join('');
    },

    /**
     * Checks if the specified string starts with a substr at the specified index.
     * @param str - String to check
     * @param startIndex - Index to start checking from
     * @param substr - Substring to check
     * @return boolean true if it does start
     */
    startsAtIndexWith(str, startIndex, substr) {
      if (str.length - startIndex < substr.length) {
        return false;
      }

      for (let i = 0; i < substr.length; i += 1) {
        if (str.charAt(startIndex + i) !== substr.charAt(i)) {
          return false;
        }
      }

      return true;
    },

    /**
     * Checks if str has unquoted substr
     * @param str
     * @param substr
     */
    hasUnquotedSubstring(str, substr) {
      const quotes = ['"', "'", '/'];
      const stack = [];

      for (let i = 0; i < str.length; i += 1) {
        const cursor = str[i];

        if (stack.length === 0) {
          if (this.startsAtIndexWith(str, i, substr)) {
            return true;
          }
        }

        if (quotes.indexOf(cursor) >= 0 && (i === 0 || str[i - 1] !== '\\')) {
          const last = stack.pop();

          if (!last) {
            stack.push(cursor);
          } else if (last !== cursor) {
            stack.push(last);
            stack.push(cursor);
          }
        }
      }

      return false;
    }

  };
  return StringUtils;
})();

/***/ }),

/***/ 8241:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

// ESM COMPAT FLAG
__webpack_require__.r(__webpack_exports__);

// EXPORTS
__webpack_require__.d(__webpack_exports__, {
  "default": () => (/* binding */ MainContainer_Main)
});

// EXTERNAL MODULE: ./node_modules/react/index.js
var react = __webpack_require__(846);
// EXTERNAL MODULE: ./node_modules/mobx-react/dist/mobxreact.esm.js + 17 modules
var mobxreact_esm = __webpack_require__(2497);
// EXTERNAL MODULE: ./node_modules/react-switch/index.js
var react_switch = __webpack_require__(7537);
// EXTERNAL MODULE: ./Extension/src/pages/popup/components/MainContainer/components/icons/Chevron.jsx
var Chevron = __webpack_require__(8610);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js
var injectStylesIntoStyleTag = __webpack_require__(5491);
var injectStylesIntoStyleTag_default = /*#__PURE__*/__webpack_require__.n(injectStylesIntoStyleTag);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/styleDomAPI.js
var styleDomAPI = __webpack_require__(9532);
var styleDomAPI_default = /*#__PURE__*/__webpack_require__.n(styleDomAPI);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/insertBySelector.js
var insertBySelector = __webpack_require__(8190);
var insertBySelector_default = /*#__PURE__*/__webpack_require__.n(insertBySelector);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/setAttributesWithoutAttributes.js
var setAttributesWithoutAttributes = __webpack_require__(7630);
var setAttributesWithoutAttributes_default = /*#__PURE__*/__webpack_require__.n(setAttributesWithoutAttributes);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/insertStyleElement.js
var insertStyleElement = __webpack_require__(664);
var insertStyleElement_default = /*#__PURE__*/__webpack_require__.n(insertStyleElement);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/styleTagTransform.js
var styleTagTransform = __webpack_require__(2563);
var styleTagTransform_default = /*#__PURE__*/__webpack_require__.n(styleTagTransform);
// EXTERNAL MODULE: ./node_modules/css-loader/dist/cjs.js??ruleSet[1].rules[3].use[1]!./node_modules/postcss-loader/dist/cjs.js!./Extension/src/pages/popup/components/MainContainer/components/Section/styles.css
var styles = __webpack_require__(1111);
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/components/Section/styles.css

      
      
      
      
      
      
      
      
      

var options = {};

options.styleTagTransform = (styleTagTransform_default());
options.setAttributes = (setAttributesWithoutAttributes_default());

      options.insert = insertBySelector_default().bind(null, "head");
    
options.domAPI = (styleDomAPI_default());
options.insertStyleElement = (insertStyleElement_default());

var update = injectStylesIntoStyleTag_default()(styles/* default */.Z, options);




       /* harmony default export */ const Section_styles = (styles/* default */.Z && styles/* default.locals */.Z.locals ? styles/* default.locals */.Z.locals : undefined);

;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/components/Section/index.jsx
/* eslint-disable jsx-a11y/interactive-supports-focus */

/* eslint-disable jsx-a11y/click-events-have-key-events */



const Section = ({
  color = '#FFF',
  background = '#000',
  backgroundHover,
  linkColor = '#FFF',
  link,
  icon = /*#__PURE__*/react.createElement("div", null),
  title,
  text,
  mb,
  onClick
}) => {
  return /*#__PURE__*/react.createElement("div", {
    className: "section",
    style: {
      background,
      color,
      marginBottom: mb,
      '--hover-color': backgroundHover || background
    },
    role: "button",
    onClick: onClick
  }, /*#__PURE__*/react.createElement("div", {
    className: "header"
  }, /*#__PURE__*/react.createElement("div", {
    className: "icon"
  }, icon), /*#__PURE__*/react.createElement("div", {
    className: "link",
    style: {
      color: linkColor
    }
  }, link, ' ', /*#__PURE__*/react.createElement(Chevron/* Chevron */.T, {
    color: linkColor
  }))), /*#__PURE__*/react.createElement("div", {
    className: "title",
    style: {
      color
    }
  }, title), /*#__PURE__*/react.createElement("div", {
    className: "text"
  }, text));
};
// EXTERNAL MODULE: ./Extension/src/pages/popup/components/MainContainer/components/Header/index.jsx + 1 modules
var Header = __webpack_require__(7798);
// EXTERNAL MODULE: ./Extension/src/pages/popup/components/MainContainer/components/IconRound/index.jsx + 1 modules
var IconRound = __webpack_require__(6747);
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/assets/settings.png
/* harmony default export */ const settings = (__webpack_require__.p + "be6f667169483255015fd1afe94ef6c4.png");
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/assets/shield.png
/* harmony default export */ const shield = (__webpack_require__.p + "e04f5e6a4c8c2b57ec1f3b4971bf0d9f.png");
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/assets/bar-chart.png
/* harmony default export */ const bar_chart = (__webpack_require__.p + "2639e8a3c80e478b1d7a5b658b92910f.png");
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/assets/qwant-logo.png
/* harmony default export */ const qwant_logo = (__webpack_require__.p + "75f7763c3b831357f2aac3ad2e12148f.png");
// EXTERNAL MODULE: ./Extension/src/pages/popup/constants.js
var constants = __webpack_require__(1592);
// EXTERNAL MODULE: ./Extension/src/common/constants.js
var common_constants = __webpack_require__(4568);
// EXTERNAL MODULE: ./Extension/src/common/common-script.js
var common_script = __webpack_require__(1351);
;// CONCATENATED MODULE: ./Extension/src/content-script/content-script.js
/**
 * This file is part of Adguard Browser Extension (https://github.com/AdguardTeam/AdguardBrowserExtension).
 *
 * Adguard Browser Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Adguard Browser Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adguard Browser Extension. If not, see <http://www.gnu.org/licenses/>.
 */

const contentPage = {
  sendMessage: common_script/* runtimeImpl.sendMessage */.V.sendMessage,
  onMessage: common_script/* runtimeImpl.onMessage */.V.onMessage
};
// EXTERNAL MODULE: ./Extension/src/background/utils/optional-permissions.js
var optional_permissions = __webpack_require__(5753);
// EXTERNAL MODULE: ./Extension/src/pages/popup/components/MainContainer/assets/check.png
var check = __webpack_require__(8801);
// EXTERNAL MODULE: ./Extension/src/background/extension-api/browser.js
var browser = __webpack_require__(2273);
// EXTERNAL MODULE: ./node_modules/css-loader/dist/cjs.js??ruleSet[1].rules[3].use[1]!./node_modules/postcss-loader/dist/cjs.js!./Extension/src/pages/popup/components/MainContainer/PermissionsMissing/styles.css
var PermissionsMissing_styles = __webpack_require__(695);
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/PermissionsMissing/styles.css

      
      
      
      
      
      
      
      
      

var styles_options = {};

styles_options.styleTagTransform = (styleTagTransform_default());
styles_options.setAttributes = (setAttributesWithoutAttributes_default());

      styles_options.insert = insertBySelector_default().bind(null, "head");
    
styles_options.domAPI = (styleDomAPI_default());
styles_options.insertStyleElement = (insertStyleElement_default());

var styles_update = injectStylesIntoStyleTag_default()(PermissionsMissing_styles/* default */.Z, styles_options);




       /* harmony default export */ const MainContainer_PermissionsMissing_styles = (PermissionsMissing_styles/* default */.Z && PermissionsMissing_styles/* default.locals */.Z.locals ? PermissionsMissing_styles/* default.locals */.Z.locals : undefined);

;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/PermissionsMissing/index.jsx






const PermissionsMissing = () => {
  const onOpenOptions = () => {
    browser/* browser.runtime.openOptionsPage */.X.runtime.openOptionsPage();
  };

  return /*#__PURE__*/react.createElement("div", {
    className: "root"
  }, /*#__PURE__*/react.createElement("img", {
    alt: "logo",
    src: qwant_logo
  }), /*#__PURE__*/react.createElement("div", {
    className: "section-default-browser"
  }, /*#__PURE__*/react.createElement("div", null, /*#__PURE__*/react.createElement("div", {
    className: "title"
  }, "Qwant est votre moteur de recherche par d\xE9faut"), /*#__PURE__*/react.createElement("div", null, "Lorsque vous lancez une recherche depuis la barre d\u2019adresse de votre navigateur")), /*#__PURE__*/react.createElement("div", null, /*#__PURE__*/react.createElement(IconRound/* IconRound */.t, {
    src: check/* default */.Z
  }))), /*#__PURE__*/react.createElement("div", {
    className: "section-enable-permissions"
  }, /*#__PURE__*/react.createElement("div", {
    className: "title"
  }, "La protection est d\xE9sactiv\xE9e"), /*#__PURE__*/react.createElement("div", null, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."), /*#__PURE__*/react.createElement("button", {
    type: "button",
    className: "cta",
    onClick: onOpenOptions
  }, "Activer la protection")));
};
// EXTERNAL MODULE: ./Extension/src/pages/popup/components/MainContainer/helpers/index.js
var helpers = __webpack_require__(9788);
// EXTERNAL MODULE: ./node_modules/css-loader/dist/cjs.js??ruleSet[1].rules[3].use[1]!./node_modules/postcss-loader/dist/cjs.js!./Extension/src/pages/popup/components/MainContainer/Main/styles.css
var Main_styles = __webpack_require__(309);
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/Main/styles.css

      
      
      
      
      
      
      
      
      

var Main_styles_options = {};

Main_styles_options.styleTagTransform = (styleTagTransform_default());
Main_styles_options.setAttributes = (setAttributesWithoutAttributes_default());

      Main_styles_options.insert = insertBySelector_default().bind(null, "head");
    
Main_styles_options.domAPI = (styleDomAPI_default());
Main_styles_options.insertStyleElement = (insertStyleElement_default());

var Main_styles_update = injectStylesIntoStyleTag_default()(Main_styles/* default */.Z, Main_styles_options);




       /* harmony default export */ const MainContainer_Main_styles = (Main_styles/* default */.Z && Main_styles/* default.locals */.Z.locals ? Main_styles/* default.locals */.Z.locals : undefined);

;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/Main/index.jsx
/* eslint-disable jsx-a11y/no-static-element-interactions */

/* eslint-disable jsx-a11y/click-events-have-key-events */


















const QWANT_URL = 'https://qwant.com';

const getProtectionLevel = ({
  protectionLevel,
  applicationFilteringDisabled
}) => {
  if (applicationFilteringDisabled) return 'disabled';
  return protectionLevel;
};

const ProtectionStatusSection = ({
  switcher,
  setViewState,
  currentSite,
  totalBlockedTab
}) => {
  const isValidURL = (0,helpers/* isWebURL */.o)(currentSite);
  const showProtectionStatus = isValidURL && (switcher.mode === 'enabled' || switcher.mode === 'disabled' || switcher.mode === 'allowlisted');

  const goToTabStats = () => {
    setViewState(constants/* VIEW_STATES.TAB_STATS */.YT.TAB_STATS);
  };

  const getBackgroundColor = () => {
    if (switcher.mode === 'enabled') {
      return {
        background: '#85d6ad',
        hover: '#a2e0c1'
      };
    }

    return {
      background: '#e9eaec',
      hover: '#f5f5f7'
    };
  };

  if (!showProtectionStatus) {
    return null;
  }

  const {
    background,
    hover
  } = getBackgroundColor();
  return /*#__PURE__*/react.createElement(Section, {
    mb: "8px",
    color: "#000",
    linkColor: "#000",
    background: background,
    backgroundHover: hover,
    icon: /*#__PURE__*/react.createElement("div", {
      onClick: e => {
        e.stopPropagation();
        e.preventDefault();
      }
    }, /*#__PURE__*/react.createElement(react_switch["default"], {
      height: 32,
      width: 56,
      handleDiameter: 24,
      onChange: () => switcher.handler(),
      checked: switcher.mode === 'enabled',
      onColor: "#fff",
      onHandleColor: "#85d6ad",
      offHandleColor: "#e9eaec",
      checkedHandleIcon: /*#__PURE__*/react.createElement("svg", {
        viewBox: "0 0 10 10",
        height: "100%",
        width: "100%"
      }, /*#__PURE__*/react.createElement("line", {
        strokeWidth: "1",
        y2: "8",
        x2: "5",
        y1: "1",
        x1: "5",
        stroke: "#000",
        fill: "none"
      })),
      uncheckedHandleIcon: /*#__PURE__*/react.createElement("svg", {
        viewBox: "0 0 10 10",
        height: "100%",
        width: "100%"
      }, /*#__PURE__*/react.createElement("ellipse", {
        ry: "3",
        rx: "3",
        cy: "4.5",
        cx: "5",
        stroke: "#000",
        fill: "none"
      })),
      checkedIcon: false,
      uncheckedIcon: false
    })),
    title: switcher.mode === 'enabled' ? 'Protection activée' : 'Protection desactivée',
    link: switcher.mode === 'enabled' ? totalBlockedTab : 0,
    onClick: goToTabStats,
    text: /*#__PURE__*/react.createElement("div", null, "\xC9l\xE9ments bloqu\xE9s", ' ', switcher.mode === 'enabled' && /*#__PURE__*/react.createElement("span", null, "sur", ' ', /*#__PURE__*/react.createElement("b", null, currentSite)), /*#__PURE__*/react.createElement("br", null), /*#__PURE__*/react.createElement("div", {
      style: {
        visibility: switcher.mode === 'enabled' ? 'visible' : 'hidden'
      }
    }, "Un probl\xE8me sur ce site ? Desactiver les protections"))
  });
};

const Main = (0,mobxreact_esm/* observer */.Pi)(({
  store,
  settingsStore
}) => {
  const [isLoading, setLoading] = react.useState(false);
  const [hasPermissions, setHasPermissions] = react.useState(null);
  const {
    applicationFilteringDisabled
  } = store;
  const {
    protectionLevel
  } = settingsStore;
  react.useEffect(() => {
    const checkRequestFilterReady = async () => {
      setLoading(true);
      const response = await contentPage.sendMessage({
        type: common_constants/* MESSAGE_TYPES.CHECK_REQUEST_FILTER_READY */.oK.CHECK_REQUEST_FILTER_READY
      });

      if (response !== null && response !== void 0 && response.ready) {
        setLoading(false);
      } else {
        setTimeout(checkRequestFilterReady, 500);
      }
    };

    checkRequestFilterReady();
  }, []);
  react.useEffect(() => {
    (async () => {
      setLoading(true);
      const isPermissionsGranted = await (0,optional_permissions/* hasAllOptionalPermissions */.R)();
      setHasPermissions(isPermissionsGranted);
      setLoading(false);
    })();
  }, []);

  const goToSettings = () => {
    store.setViewState(constants/* VIEW_STATES.SETTINGS */.YT.SETTINGS);
  };

  const goToGlobalStats = () => {
    store.setViewState(constants/* VIEW_STATES.GLOBAL_STATS */.YT.GLOBAL_STATS);
  };

  const onReload = () => {
    browser/* browser.runtime.reload */.X.runtime.reload();
  };

  const openTab = url => {
    browser/* browser.tabs.create */.X.tabs.create({
      active: true,
      url
    });
  };

  if (hasPermissions === false) {
    return /*#__PURE__*/react.createElement(PermissionsMissing, null);
  }

  if (isLoading || !store.isInitialDataReceived) {
    return /*#__PURE__*/react.createElement("div", {
      className: "main"
    }, /*#__PURE__*/react.createElement("h1", null, "Loading..."), /*#__PURE__*/react.createElement("div", null, "Stuck?", ' ', /*#__PURE__*/react.createElement("button", {
      type: "button",
      onClick: onReload
    }, "Reload")));
  }

  const switchersMap = {
    [constants/* POPUP_STATES.APPLICATION_ENABLED */.Zs.APPLICATION_ENABLED]: {
      handler: () => {
        store.toggleAllowlisted();
      },
      mode: 'enabled'
    },
    [constants/* POPUP_STATES.APPLICATION_FILTERING_DISABLED */.Zs.APPLICATION_FILTERING_DISABLED]: {
      handler: () => {
        store.changeApplicationFilteringDisabled(false);
      },
      mode: 'disabled'
    },
    [constants/* POPUP_STATES.APPLICATION_UNAVAILABLE */.Zs.APPLICATION_UNAVAILABLE]: {
      mode: 'unavailable'
    },
    [constants/* POPUP_STATES.SITE_IN_EXCEPTION */.Zs.SITE_IN_EXCEPTION]: {
      mode: 'in-exception'
    },
    [constants/* POPUP_STATES.SITE_ALLOWLISTED */.Zs.SITE_ALLOWLISTED]: {
      handler: () => {
        store.toggleAllowlisted();
      },
      mode: 'allowlisted'
    }
  };
  const switcher = switchersMap[store.popupState];
  const level = getProtectionLevel({
    protectionLevel,
    applicationFilteringDisabled
  });
  const annoyanceTime = (0,helpers/* formatAnnoyanceTime */.a)(store.totalBlocked);
  return /*#__PURE__*/react.createElement("div", {
    className: "main"
  }, /*#__PURE__*/react.createElement(Header/* Header */.h, null, /*#__PURE__*/react.createElement("div", {
    className: "logo",
    onClick: () => openTab(QWANT_URL)
  }, /*#__PURE__*/react.createElement("img", {
    alt: "logo",
    src: qwant_logo
  })), /*#__PURE__*/react.createElement("div", {
    className: "settings",
    onClick: goToSettings
  }, /*#__PURE__*/react.createElement("img", {
    alt: "Settings",
    src: settings
  }))), /*#__PURE__*/react.createElement(ProtectionStatusSection, {
    switcher: switcher,
    setViewState: store.setViewState,
    currentSite: store.currentSite,
    totalBlockedTab: store.totalBlockedTab
  }), /*#__PURE__*/react.createElement(Section, {
    mb: "8px",
    color: "#FFF",
    linkColor: "#99beff",
    backgroundHover: "#292929",
    background: "#000",
    icon: /*#__PURE__*/react.createElement(IconRound/* IconRound */.t, {
      background: "#fff",
      src: shield
    }),
    title: "Niveau de protection",
    link: level,
    onClick: goToSettings,
    text: /*#__PURE__*/react.createElement(react.Fragment, null, level === 'standard' && 'Bloque la plupart des cookies & trackers efficacement', level === 'strict' && 'Bloque tous les trackers, mais pourrait casser certains sites', level === 'disabled' && 'L’extension met uniquement Qwant en moteur de recherche par défaut')
  }), /*#__PURE__*/react.createElement(Section, {
    color: "#000",
    linkColor: "#000",
    background: "#ded6ff",
    backgroundHover: "#eeeaff",
    icon: /*#__PURE__*/react.createElement(IconRound/* IconRound */.t, {
      background: "#000",
      src: bar_chart
    }),
    title: "Vos statistiques",
    link: "",
    onClick: goToGlobalStats,
    text: /*#__PURE__*/react.createElement("div", {
      className: "stats-wrapper"
    }, /*#__PURE__*/react.createElement("div", null, /*#__PURE__*/react.createElement("div", null, "Trackers bloqu\xE9s"), /*#__PURE__*/react.createElement("div", {
      className: "metric"
    }, store.totalBlocked)), /*#__PURE__*/react.createElement("div", null, /*#__PURE__*/react.createElement("div", null, "Temps \xE9conomis\xE9"), /*#__PURE__*/react.createElement("div", {
      className: "metric"
    }, annoyanceTime)))
  }));
});
/* harmony default export */ const MainContainer_Main = (Main);

/***/ }),

/***/ 7798:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {


// EXPORTS
__webpack_require__.d(__webpack_exports__, {
  "h": () => (/* binding */ Header)
});

// EXTERNAL MODULE: ./node_modules/react/index.js
var react = __webpack_require__(846);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js
var injectStylesIntoStyleTag = __webpack_require__(5491);
var injectStylesIntoStyleTag_default = /*#__PURE__*/__webpack_require__.n(injectStylesIntoStyleTag);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/styleDomAPI.js
var styleDomAPI = __webpack_require__(9532);
var styleDomAPI_default = /*#__PURE__*/__webpack_require__.n(styleDomAPI);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/insertBySelector.js
var insertBySelector = __webpack_require__(8190);
var insertBySelector_default = /*#__PURE__*/__webpack_require__.n(insertBySelector);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/setAttributesWithoutAttributes.js
var setAttributesWithoutAttributes = __webpack_require__(7630);
var setAttributesWithoutAttributes_default = /*#__PURE__*/__webpack_require__.n(setAttributesWithoutAttributes);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/insertStyleElement.js
var insertStyleElement = __webpack_require__(664);
var insertStyleElement_default = /*#__PURE__*/__webpack_require__.n(insertStyleElement);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/styleTagTransform.js
var styleTagTransform = __webpack_require__(2563);
var styleTagTransform_default = /*#__PURE__*/__webpack_require__.n(styleTagTransform);
// EXTERNAL MODULE: ./node_modules/css-loader/dist/cjs.js??ruleSet[1].rules[3].use[1]!./node_modules/postcss-loader/dist/cjs.js!./Extension/src/pages/popup/components/MainContainer/components/Header/styles.css
var styles = __webpack_require__(6586);
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/components/Header/styles.css

      
      
      
      
      
      
      
      
      

var options = {};

options.styleTagTransform = (styleTagTransform_default());
options.setAttributes = (setAttributesWithoutAttributes_default());

      options.insert = insertBySelector_default().bind(null, "head");
    
options.domAPI = (styleDomAPI_default());
options.insertStyleElement = (insertStyleElement_default());

var update = injectStylesIntoStyleTag_default()(styles/* default */.Z, options);




       /* harmony default export */ const Header_styles = (styles/* default */.Z && styles/* default.locals */.Z.locals ? styles/* default.locals */.Z.locals : undefined);

;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/components/Header/index.jsx


const Header = ({
  children
}) => {
  return /*#__PURE__*/react.createElement("div", {
    className: "header"
  }, children);
};

/***/ }),

/***/ 6747:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {


// EXPORTS
__webpack_require__.d(__webpack_exports__, {
  "t": () => (/* binding */ IconRound)
});

// EXTERNAL MODULE: ./node_modules/react/index.js
var react = __webpack_require__(846);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js
var injectStylesIntoStyleTag = __webpack_require__(5491);
var injectStylesIntoStyleTag_default = /*#__PURE__*/__webpack_require__.n(injectStylesIntoStyleTag);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/styleDomAPI.js
var styleDomAPI = __webpack_require__(9532);
var styleDomAPI_default = /*#__PURE__*/__webpack_require__.n(styleDomAPI);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/insertBySelector.js
var insertBySelector = __webpack_require__(8190);
var insertBySelector_default = /*#__PURE__*/__webpack_require__.n(insertBySelector);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/setAttributesWithoutAttributes.js
var setAttributesWithoutAttributes = __webpack_require__(7630);
var setAttributesWithoutAttributes_default = /*#__PURE__*/__webpack_require__.n(setAttributesWithoutAttributes);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/insertStyleElement.js
var insertStyleElement = __webpack_require__(664);
var insertStyleElement_default = /*#__PURE__*/__webpack_require__.n(insertStyleElement);
// EXTERNAL MODULE: ./node_modules/style-loader/dist/runtime/styleTagTransform.js
var styleTagTransform = __webpack_require__(2563);
var styleTagTransform_default = /*#__PURE__*/__webpack_require__.n(styleTagTransform);
// EXTERNAL MODULE: ./node_modules/css-loader/dist/cjs.js??ruleSet[1].rules[3].use[1]!./node_modules/postcss-loader/dist/cjs.js!./Extension/src/pages/popup/components/MainContainer/components/IconRound/styles.css
var styles = __webpack_require__(2476);
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/components/IconRound/styles.css

      
      
      
      
      
      
      
      
      

var options = {};

options.styleTagTransform = (styleTagTransform_default());
options.setAttributes = (setAttributesWithoutAttributes_default());

      options.insert = insertBySelector_default().bind(null, "head");
    
options.domAPI = (styleDomAPI_default());
options.insertStyleElement = (insertStyleElement_default());

var update = injectStylesIntoStyleTag_default()(styles/* default */.Z, options);




       /* harmony default export */ const IconRound_styles = (styles/* default */.Z && styles/* default.locals */.Z.locals ? styles/* default.locals */.Z.locals : undefined);

;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/components/IconRound/index.jsx


const IconRound = ({
  className,
  src,
  background = '#000'
}) => /*#__PURE__*/react.createElement("div", {
  className: `icon-round ${className}`,
  style: {
    background
  }
}, /*#__PURE__*/react.createElement("img", {
  alt: "icon",
  src: src
}));

/***/ }),

/***/ 8610:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "T": () => (/* binding */ Chevron)
/* harmony export */ });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(846);

const Chevron = ({
  className = '',
  color = '#0C0C0E'
}) => /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0__.createElement("svg", {
  className: className,
  width: "24",
  height: "24",
  xmlns: "http://www.w3.org/2000/svg"
}, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0__.createElement("path", {
  d: "m15.7 12.7-6 6c-.2.2-.4.3-.7.3-.3 0-.5-.1-.7-.3-.4-.4-.4-1 0-1.4l5.3-5.3-5.3-5.3c-.4-.4-.4-1 0-1.4.4-.4 1-.4 1.4 0l6 6c.4.4.4 1 0 1.4z",
  fill: color,
  fillRule: "evenodd"
}));

/***/ }),

/***/ 9788:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "a": () => (/* binding */ formatAnnoyanceTime),
/* harmony export */   "o": () => (/* binding */ isWebURL)
/* harmony export */ });
const isWebURL = url => {
  if (!url) return false;
  const regex = new RegExp(/^(http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([-.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/);
  return regex.test(url);
};
const formatAnnoyanceTime = totalBlocked => {
  const annoyanceTimeSeconds = Math.ceil(totalBlocked * 0.005);
  return `${annoyanceTimeSeconds} s`;
};

/***/ }),

/***/ 309:
/***/ ((module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Z": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony import */ var _node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(1389);
/* harmony import */ var _node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(9633);
/* harmony import */ var _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1__);
// Imports


var ___CSS_LOADER_EXPORT___ = _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1___default()((_node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0___default()));
// Module
___CSS_LOADER_EXPORT___.push([module.id, ".stats-wrapper {\n    display: flex;\n    align-content: space-between;\n}\n\n.stats-wrapper>div {\n    flex: 1;\n    font-size: 14px;\n    line-height: 18px;\n}\n\n.stats-wrapper>div>.metric {\n    font-weight: bold;\n    font-size: 20px;\n    line-height: 24px;\n    letter-spacing: -0.4px;\n}\n\n.logo, .settings {\n    -webkit-user-select: none;\n       -moz-user-select: none;\n        -ms-user-select: none;\n            user-select: none;\n}\n\n.logo img, .settings img {\n    cursor: pointer;\n}", ""]);
// Exports
/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (___CSS_LOADER_EXPORT___);


/***/ }),

/***/ 695:
/***/ ((module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Z": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony import */ var _node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(1389);
/* harmony import */ var _node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(9633);
/* harmony import */ var _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1__);
// Imports


var ___CSS_LOADER_EXPORT___ = _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1___default()((_node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0___default()));
// Module
___CSS_LOADER_EXPORT___.push([module.id, ".section-default-browser {\n  background-color: var(--green);\n  padding: 16px;\n  border: solid 2px black;\n  border-radius: 8px;\n  display: flex;\n  align-items: center;\n\n  font-size: 14px;\n  line-height: 1.29;\n  color: var(--grey-black);\n  margin-top: 24px;\n  margin-bottom: 8px;\n}\n\n.section-default-browser > div:first-child {\n  padding-right: 36px;\n}\n\n.section-default-browser .title {\n  margin: 0 32px 4px 0;\n  font-size: 20px;\n  font-weight: bold;\n  line-height: 1.2;\n  letter-spacing: -0.4px;\n  margin-bottom: 8px;\n}\n\n.section-enable-permissions {\n  background-color: var(--grey);\n  padding: 16px;\n  border: solid 2px black;\n  border-radius: 8px;\n}\n\n.section-enable-permissions .title {\n  margin: 0 0 8px;\n  font-size: 28px;\n  font-weight: bold;\n  line-height: 1.14;\n  letter-spacing: -0.8px;\n  color: var(--grey-black);\n}\n\n.section-enable-permissions .cta {\n  width: 155px;\n  height: 36px;\n  margin: 16px 189px 0 0;\n  border-radius: 8px;\n  border: none;\n  background-color: var(--grey-black);\n\n  font-size: 14px;\n  line-height: 1.29;\n  color: var(--grey-bright);\n  cursor: pointer;\n}\n\n.section-enable-permissions .cta:hover {\n  background-color: #29292f;\n}\n", ""]);
// Exports
/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (___CSS_LOADER_EXPORT___);


/***/ }),

/***/ 6586:
/***/ ((module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Z": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony import */ var _node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(1389);
/* harmony import */ var _node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(9633);
/* harmony import */ var _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1__);
// Imports


var ___CSS_LOADER_EXPORT___ = _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1___default()((_node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0___default()));
// Module
___CSS_LOADER_EXPORT___.push([module.id, ".header {\n  display: flex;\n  align-items: center;\n  align-content: space-between;\n  width: 100%;\n  justify-content: space-between;\n  margin-bottom: 14px;\n}\n", ""]);
// Exports
/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (___CSS_LOADER_EXPORT___);


/***/ }),

/***/ 2476:
/***/ ((module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Z": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony import */ var _node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(1389);
/* harmony import */ var _node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(9633);
/* harmony import */ var _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1__);
// Imports


var ___CSS_LOADER_EXPORT___ = _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1___default()((_node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0___default()));
// Module
___CSS_LOADER_EXPORT___.push([module.id, ".icon-round {\n  width: 32px;\n  height: 32px;\n  border-radius: 26px;\n  display: flex;\n  justify-content: center;\n  align-items: center;\n  padding: 4px;\n}\n\n.icon-round img {\n  width: 24px;\n  height: 24px;\n  -o-object-fit: contain;\n     object-fit: contain;\n}\n", ""]);
// Exports
/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (___CSS_LOADER_EXPORT___);


/***/ }),

/***/ 1111:
/***/ ((module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Z": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony import */ var _node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(1389);
/* harmony import */ var _node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(9633);
/* harmony import */ var _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1__);
// Imports


var ___CSS_LOADER_EXPORT___ = _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1___default()((_node_modules_css_loader_dist_runtime_noSourceMaps_js__WEBPACK_IMPORTED_MODULE_0___default()));
// Module
___CSS_LOADER_EXPORT___.push([module.id, ".section {\n    background-color: var(--green);\n    padding: 12px 16px;\n    border: solid 2px black;\n    border-radius: 8px;\n    cursor: pointer;\n    transition: background-color 0.15s ease-in-out;\n}\n\n.section:hover {\n    background-color: var(--hover-color) !important;\n}\n\n.section .link svg {\n    transition: transform 0.15s ease-in-out;\n}\n\n.section:hover .link svg {\n    transform: scale(1.1) translateX(2px);\n}\n\n.section .icon img {\n    transition: transform 0.2s ease-in-out;\n}\n\n.section:hover .icon img {\n    transform: scale(1.1);\n}\n\n.section>.header {\n    width: 100%;\n    display: flex;\n    align-items: center;\n    align-content: space-between;\n    justify-content: space-between;\n    margin-bottom: 16px;\n\n    font-size: 28px;\n    font-weight: bold;\n    line-height: 1.14;\n    letter-spacing: -0.8px;\n}\n\n.section>.header>.link {\n    display: flex;\n    align-items: center;\n    font-weight: bold;\n    font-size: 20px;\n    line-height: 24px;\n    letter-spacing: -0.4px;\n    text-transform: capitalize;\n}\n\n.section>.title {\n    font-weight: bold;\n    font-size: 28px;\n    line-height: 32px;\n    letter-spacing: -0.8px;\n    color: var(--grey-black);\n    margin-bottom: 8px;\n}\n\n.section>.text {\n    font-size: 14px;\n    line-height: 18px;\n}", ""]);
// Exports
/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (___CSS_LOADER_EXPORT___);


/***/ }),

/***/ 8801:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Z": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (__webpack_require__.p + "cf855e1a663248fd7eabc5d999838fc2.png");

/***/ })

}]);