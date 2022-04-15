"use strict";
(self["webpackChunkqwant_privacy_pilot"] = self["webpackChunkqwant_privacy_pilot"] || []).push([[560],{

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

/***/ 4560:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

// ESM COMPAT FLAG
__webpack_require__.r(__webpack_exports__);

// EXPORTS
__webpack_require__.d(__webpack_exports__, {
  "default": () => (/* binding */ MainContainer_SettingsView)
});

// EXTERNAL MODULE: ./node_modules/@babel/runtime/helpers/esm/extends.js
var esm_extends = __webpack_require__(3229);
// EXTERNAL MODULE: ./node_modules/react/index.js
var react = __webpack_require__(846);
// EXTERNAL MODULE: ./node_modules/mobx-react/dist/mobxreact.esm.js + 17 modules
var mobxreact_esm = __webpack_require__(2497);
// EXTERNAL MODULE: ./Extension/src/pages/popup/components/MainContainer/components/Header/index.jsx + 1 modules
var Header = __webpack_require__(7798);
// EXTERNAL MODULE: ./Extension/src/pages/popup/components/MainContainer/components/GoBack/index.jsx + 2 modules
var GoBack = __webpack_require__(3909);
// EXTERNAL MODULE: ./Extension/src/pages/popup/components/MainContainer/components/Title/index.jsx + 1 modules
var Title = __webpack_require__(4664);
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
// EXTERNAL MODULE: ./node_modules/css-loader/dist/cjs.js??ruleSet[1].rules[3].use[1]!./node_modules/postcss-loader/dist/cjs.js!./Extension/src/pages/popup/components/MainContainer/SettingsView/styles.css
var styles = __webpack_require__(2747);
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/SettingsView/styles.css

      
      
      
      
      
      
      
      
      

var options = {};

options.styleTagTransform = (styleTagTransform_default());
options.setAttributes = (setAttributesWithoutAttributes_default());

      options.insert = insertBySelector_default().bind(null, "head");
    
options.domAPI = (styleDomAPI_default());
options.insertStyleElement = (insertStyleElement_default());

var update = injectStylesIntoStyleTag_default()(styles/* default */.Z, options);




       /* harmony default export */ const SettingsView_styles = (styles/* default */.Z && styles/* default.locals */.Z.locals ? styles/* default.locals */.Z.locals : undefined);

// EXTERNAL MODULE: ./Extension/src/pages/popup/components/MainContainer/assets/check.png
var check = __webpack_require__(8801);
// EXTERNAL MODULE: ./Extension/src/pages/popup/components/MainContainer/components/IconRound/index.jsx + 1 modules
var IconRound = __webpack_require__(6747);
// EXTERNAL MODULE: ./Extension/src/pages/popup/components/MainContainer/components/icons/Chevron.jsx
var Chevron = __webpack_require__(8610);
// EXTERNAL MODULE: ./Extension/src/pages/popup/constants.js
var constants = __webpack_require__(1592);
// EXTERNAL MODULE: ./Extension/src/pages/services/messenger.js
var messenger = __webpack_require__(7916);
// EXTERNAL MODULE: ./Extension/src/background/extension-api/browser.js
var browser = __webpack_require__(2273);
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/SettingsView/index.jsx


/* eslint-disable jsx-a11y/click-events-have-key-events */

/* eslint-disable jsx-a11y/interactive-supports-focus */













const ProtectionButton = ({
  text,
  description,
  onClick,
  active
}) => {
  return /*#__PURE__*/react.createElement("div", {
    role: "button",
    className: `protection-list__button ${active && 'protection-list__button--active'}`,
    onClick: onClick
  }, /*#__PURE__*/react.createElement("div", {
    className: "inner-left"
  }, /*#__PURE__*/react.createElement("span", {
    className: "inner-left__title"
  }, text), /*#__PURE__*/react.createElement("span", {
    className: "inner-left__desc"
  }, description)), /*#__PURE__*/react.createElement("div", {
    className: "inner-right"
  }, /*#__PURE__*/react.createElement(IconRound/* IconRound */.t, {
    src: check/* default */.Z,
    className: `inner-right__active-check ${active && 'inner-right__active-check--visible'}`
  })));
};

const HelpLink = ({
  url,
  text
}) => {
  const openTab = () => {
    browser/* browser.tabs.create */.X.tabs.create({
      active: true,
      url
    });
  };

  return /*#__PURE__*/react.createElement("a", {
    href: "#url",
    onClick: openTab,
    title: text
  }, /*#__PURE__*/react.createElement("span", null, text), /*#__PURE__*/react.createElement(Chevron/* Chevron */.T, {
    className: "chevron"
  }));
};

const protectionLevels = [{
  id: 'standard',
  text: 'Standard',
  description: 'Bloque la plupart des cookies & trackers efficacement'
}, {
  id: 'strict',
  text: 'Strict',
  description: 'Bloque tous les trackers, mais pourrait casser certains sites'
}, {
  id: 'disabled',
  text: 'Desactivé',
  description: 'L’extension met uniquement Qwant en moteur de recherche par défaut'
}];
const helpLinks = [{
  text: "What's new",
  url: 'http://wikipedia.org/2'
}, {
  text: 'Give feedback to developer',
  url: 'https://pad.qwant.ninja/p/web-ext-qpp-alpha-feedback'
}];
const SettingsView = (0,mobxreact_esm/* observer */.Pi)(({
  store,
  settingsStore
}) => {
  const {
    applicationFilteringDisabled
  } = store;
  const {
    protectionLevel
  } = settingsStore;

  if (helpLinks.length === 2) {
    helpLinks.push({
      text: `v${settingsStore.version}`,
      url: 'https://qwant.com'
    });
  }

  const [activeLevel, setActiveLevel] = react.useState(() => {
    if (applicationFilteringDisabled) return 'disabled';
    return protectionLevel;
  });
  const [, setLoading] = react.useState(false);

  const goToMain = () => {
    store.setViewState(constants/* VIEW_STATES.MAIN */.YT.MAIN);
  };

  const onProtectionDisable = async () => {
    await store.changeApplicationFilteringDisabled(!applicationFilteringDisabled);
  };

  const onProtectionLevelChange = async value => {
    setActiveLevel(value);

    if (value === 'disabled') {
      await onProtectionDisable();
      return;
    }

    setLoading(true);
    await store.changeApplicationFilteringDisabled(false);
    messenger/* messenger.changeProtectionLevel */.d.changeProtectionLevel(value);
    settingsStore.setProtectionLevel(value);
    setLoading(false);
  };

  return /*#__PURE__*/react.createElement("div", {
    className: "root"
  }, /*#__PURE__*/react.createElement(Header/* Header */.h, null, /*#__PURE__*/react.createElement(GoBack/* GoBack */.k, {
    onClick: goToMain
  })), /*#__PURE__*/react.createElement(Title/* Title */.D, null, "Niveau de protection"), /*#__PURE__*/react.createElement("div", {
    className: "protection-list"
  }, protectionLevels.map(level => /*#__PURE__*/react.createElement(ProtectionButton, (0,esm_extends/* default */.Z)({}, level, {
    key: level.id,
    active: activeLevel === level.id,
    onClick: () => onProtectionLevelChange(level.id)
  })))), /*#__PURE__*/react.createElement(Title/* Title */.D, null, "Aide"), /*#__PURE__*/react.createElement("div", {
    className: "help-links"
  }, helpLinks.map(link => /*#__PURE__*/react.createElement(HelpLink, (0,esm_extends/* default */.Z)({}, link, {
    key: link.url
  })))));
});
/* harmony default export */ const MainContainer_SettingsView = (SettingsView);

/***/ }),

/***/ 3909:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {


// EXPORTS
__webpack_require__.d(__webpack_exports__, {
  "k": () => (/* binding */ GoBack)
});

// EXTERNAL MODULE: ./node_modules/react/index.js
var react = __webpack_require__(846);
// EXTERNAL MODULE: ./Extension/src/pages/popup/components/MainContainer/components/IconRound/index.jsx + 1 modules
var IconRound = __webpack_require__(6747);
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/assets/arrow-backward.png
/* harmony default export */ const arrow_backward = (__webpack_require__.p + "3b09812651d59957d21472392ecafe74.png");
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
// EXTERNAL MODULE: ./node_modules/css-loader/dist/cjs.js??ruleSet[1].rules[3].use[1]!./node_modules/postcss-loader/dist/cjs.js!./Extension/src/pages/popup/components/MainContainer/components/GoBack/styles.css
var styles = __webpack_require__(7084);
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/components/GoBack/styles.css

      
      
      
      
      
      
      
      
      

var options = {};

options.styleTagTransform = (styleTagTransform_default());
options.setAttributes = (setAttributesWithoutAttributes_default());

      options.insert = insertBySelector_default().bind(null, "head");
    
options.domAPI = (styleDomAPI_default());
options.insertStyleElement = (insertStyleElement_default());

var update = injectStylesIntoStyleTag_default()(styles/* default */.Z, options);




       /* harmony default export */ const GoBack_styles = (styles/* default */.Z && styles/* default.locals */.Z.locals ? styles/* default.locals */.Z.locals : undefined);

;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/components/GoBack/index.jsx




const GoBack = ({
  goBackText = 'Retour',
  onClick
}) => /*#__PURE__*/react.createElement("button", {
  type: "button",
  className: "back-btn",
  onClick: onClick
}, /*#__PURE__*/react.createElement(IconRound/* IconRound */.t, {
  src: arrow_backward,
  background: "#FFF"
}), /*#__PURE__*/react.createElement("span", {
  className: "back-btn__text"
}, goBackText));

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

/***/ 4664:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {


// EXPORTS
__webpack_require__.d(__webpack_exports__, {
  "D": () => (/* binding */ Title)
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
// EXTERNAL MODULE: ./node_modules/css-loader/dist/cjs.js??ruleSet[1].rules[3].use[1]!./node_modules/postcss-loader/dist/cjs.js!./Extension/src/pages/popup/components/MainContainer/components/Title/styles.css
var styles = __webpack_require__(8239);
;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/components/Title/styles.css

      
      
      
      
      
      
      
      
      

var options = {};

options.styleTagTransform = (styleTagTransform_default());
options.setAttributes = (setAttributesWithoutAttributes_default());

      options.insert = insertBySelector_default().bind(null, "head");
    
options.domAPI = (styleDomAPI_default());
options.insertStyleElement = (insertStyleElement_default());

var update = injectStylesIntoStyleTag_default()(styles/* default */.Z, options);




       /* harmony default export */ const Title_styles = (styles/* default */.Z && styles/* default.locals */.Z.locals ? styles/* default.locals */.Z.locals : undefined);

;// CONCATENATED MODULE: ./Extension/src/pages/popup/components/MainContainer/components/Title/index.jsx


const Title = ({
  children
}) => /*#__PURE__*/react.createElement("div", {
  className: "title"
}, children);

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

/***/ 2747:
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
___CSS_LOADER_EXPORT___.push([module.id, ".protection-list {\n    margin-bottom: 16px;\n}\n\n.protection-list__button {\n    display: flex;\n    justify-content: space-between;\n    border: 2px solid var(--grey-base);\n    padding: 16px;\n    border-radius: 8px;\n    margin-bottom: 6px;\n    transition: background 0.15s ease-in-out, border 0.15s ease-in-out;\n    -webkit-user-select: none;\n       -moz-user-select: none;\n        -ms-user-select: none;\n            user-select: none;\n}\n\n.protection-list__button:hover:not(.protection-list__button--active) {\n    background: var(--grey-bright);\n}\n\n.protection-list__button--active {\n    background: var(--blue-light);\n    border-color: var(--grey-black);\n}\n\n.protection-list__button:last-child {\n    margin-bottom: 0px;\n}\n\n.protection-list__button:hover {\n    cursor: pointer;\n}\n\n.inner-left {\n    display: flex;\n    width: 270px;\n    flex-direction: column;\n}\n\n.inner-left__title {\n    font-weight: bold;\n    font-size: 20px;\n    color: var(--grey-black);\n    margin-bottom: 4px;\n}\n\n.inner-left__desc {\n    font-size: 14px;\n    color: var(--grey-semi-darkness);\n}\n\n.protection-list__button--active .inner-left__desc {\n    color: var(--grey-black);\n}\n\n.inner-right {\n    display: flex;\n    align-items: center;\n    justify-content: center;\n}\n\n.inner-right__active-check {\n    padding: 0;\n    opacity: 0;\n    transition: opacity 0.2s ease-in-out;\n}\n\n.inner-right__active-check img {\n    width: 20px;\n}\n\n.inner-right__active-check--visible {\n    opacity: 1;\n}\n\n.protection-list__button:hover:not(.protection-list__button--active) .inner-right__active-check {\n    opacity: 0.1;\n}\n\n.help-links {\n    display: flex;\n    flex-direction: column;\n    border-radius: 8px;\n    border: 2px solid var(--grey-black);\n    overflow: hidden;\n}\n\n.help-links a {\n    display: flex;\n    justify-content: space-between;\n    align-items: center;\n    color: var(--grey-black);\n    font-size: 16px;\n    font-weight: bold;\n    text-decoration: none;\n    border-bottom: 1px solid var(--grey-semi-darkness);\n    padding: 8px 12px;\n    transition: background-color 0.15s ease-in-out;\n}\n\n.help-links a .chevron {\n    transition: transform 0.15s ease-in-out;\n}\n\n.help-links a:hover {\n    background: var(--grey-bright);\n}\n\n.help-links a:hover .chevron {\n    transform: translateX(2px);\n}\n\n.help-links a:last-child {\n    border-bottom: none;\n}", ""]);
// Exports
/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (___CSS_LOADER_EXPORT___);


/***/ }),

/***/ 7084:
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
___CSS_LOADER_EXPORT___.push([module.id, ".back-btn {\n  display: flex;\n  background: none;\n  align-items: center;\n  justify-content: flex-start;\n  border: none;\n  padding: 0;\n}\n\n.back-btn:hover {\n  cursor: pointer;\n}\n\n.back-btn .icon-round {\n  transition: 0.15s ease-in-out;\n}\n\n.back-btn:hover .icon-round {\n  transform: translateX(-2px);\n}\n\n.back-btn__text {\n  font-size: 16px;\n  line-height: 20px;\n}", ""]);
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

/***/ 8239:
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
___CSS_LOADER_EXPORT___.push([module.id, ".title {\n  font-size: 28px;\n  font-weight: bold;\n  font-stretch: normal;\n  font-style: normal;\n  line-height: 1.14;\n  letter-spacing: -0.8px;\n  color: var(--grey-black);\n  margin-bottom: 10px;\n}\n", ""]);
// Exports
/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (___CSS_LOADER_EXPORT___);


/***/ }),

/***/ 8801:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Z": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (__webpack_require__.p + "cf855e1a663248fd7eabc5d999838fc2.png");

/***/ }),

/***/ 3229:
/***/ ((__unused_webpack___webpack_module__, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Z": () => (/* binding */ _extends)
/* harmony export */ });
function _extends() {
  _extends = Object.assign || function (target) {
    for (var i = 1; i < arguments.length; i++) {
      var source = arguments[i];

      for (var key in source) {
        if (Object.prototype.hasOwnProperty.call(source, key)) {
          target[key] = source[key];
        }
      }
    }

    return target;
  };

  return _extends.apply(this, arguments);
}

/***/ })

}]);