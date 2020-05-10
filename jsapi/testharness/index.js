
'use strict';

const indigo = require('../out/indigojs/fastOpt/dest/out.js');

const config =
    indigo.GameConfigHelper.default
        .withViewport(new indigo.GameViewport(550, 400))
        .withClearColor(new indigo.ClearColor(0.5, 0, 0.5, 1))
        .withFrameRate(60)
        .withMagnification(1);

const fontKey = 'My Font';
const fontName = 'boxyFont';
const spriteName = 'trafficLights';
const graphicName = 'graphics';

const assets = [
  new indigo.ImageAsset(fontName, 'assets/boxy_font.png'),
  new indigo.ImageAsset(spriteName, 'assets/trafficlights.png'),
  new indigo.ImageAsset(graphicName, 'assets/graphics.png'),
];

const fontChars = [
  new indigo.FontChar('A', new indigo.Rectangle(3, 78, 23, 23)),
  new indigo.FontChar('B', new indigo.Rectangle(26, 78, 23, 23)),
  new indigo.FontChar('C', new indigo.Rectangle(50, 78, 23, 23)),
  new indigo.FontChar('D', new indigo.Rectangle(73, 78, 23, 23)),
  new indigo.FontChar('E', new indigo.Rectangle(96, 78, 23, 23)),
  new indigo.FontChar('F', new indigo.Rectangle(119, 78, 23, 23)),
  new indigo.FontChar('G', new indigo.Rectangle(142, 78, 23, 23)),
  new indigo.FontChar('H', new indigo.Rectangle(165, 78, 23, 23)),
  new indigo.FontChar('I', new indigo.Rectangle(188, 78, 15, 23)),
  new indigo.FontChar('J', new indigo.Rectangle(202, 78, 23, 23)),
  new indigo.FontChar('K', new indigo.Rectangle(225, 78, 23, 23)),
  new indigo.FontChar('L', new indigo.Rectangle(248, 78, 23, 23)),
  new indigo.FontChar('M', new indigo.Rectangle(271, 78, 23, 23)),
  new indigo.FontChar('N', new indigo.Rectangle(3, 104, 23, 23)),
  new indigo.FontChar('O', new indigo.Rectangle(29, 104, 23, 23)),
  new indigo.FontChar('P', new indigo.Rectangle(54, 104, 23, 23)),
  new indigo.FontChar('Q', new indigo.Rectangle(75, 104, 23, 23)),
  new indigo.FontChar('R', new indigo.Rectangle(101, 104, 23, 23)),
  new indigo.FontChar('S', new indigo.Rectangle(124, 104, 23, 23)),
  new indigo.FontChar('T', new indigo.Rectangle(148, 104, 23, 23)),
  new indigo.FontChar('U', new indigo.Rectangle(173, 104, 23, 23)),
  new indigo.FontChar('V', new indigo.Rectangle(197, 104, 23, 23)),
  new indigo.FontChar('W', new indigo.Rectangle(220, 104, 23, 23)),
  new indigo.FontChar('X', new indigo.Rectangle(248, 104, 23, 23)),
  new indigo.FontChar('Y', new indigo.Rectangle(271, 104, 23, 23)),
  new indigo.FontChar('Z', new indigo.Rectangle(297, 104, 23, 23)),
  new indigo.FontChar('0', new indigo.Rectangle(3, 26, 23, 23)),
  new indigo.FontChar('1', new indigo.Rectangle(26, 26, 15, 23)),
  new indigo.FontChar('2', new indigo.Rectangle(41, 26, 23, 23)),
  new indigo.FontChar('3', new indigo.Rectangle(64, 26, 23, 23)),
  new indigo.FontChar('4', new indigo.Rectangle(87, 26, 23, 23)),
  new indigo.FontChar('5', new indigo.Rectangle(110, 26, 23, 23)),
  new indigo.FontChar('6', new indigo.Rectangle(133, 26, 23, 23)),
  new indigo.FontChar('7', new indigo.Rectangle(156, 26, 23, 23)),
  new indigo.FontChar('8', new indigo.Rectangle(179, 26, 23, 23)),
  new indigo.FontChar('9', new indigo.Rectangle(202, 26, 23, 23)),
  new indigo.FontChar('?', new indigo.Rectangle(93, 52, 23, 23)),
  new indigo.FontChar('!', new indigo.Rectangle(3, 0, 15, 23)),
  new indigo.FontChar('.', new indigo.Rectangle(286, 0, 15, 23)),
  new indigo.FontChar(',', new indigo.Rectangle(248, 0, 15, 23)),
  new indigo.FontChar(' ', new indigo.Rectangle(145, 52, 23, 23)),
];

const fonts = [
  new indigo.FontInfo(
      fontKey,
      new indigo.Textured(fontName, false),
      320,
      230,
      new indigo.FontChar('?', new indigo.Rectangle(93, 52, 23, 23)),
      fontChars,
      false,
  ),
];

const cycles = [
  new indigo.Cycle(
      'lights',
      [
        new indigo.Frame(new indigo.Rectangle(0, 0, 64, 64), 250),
        new indigo.Frame(new indigo.Rectangle(64, 0, 64, 64), 250),
        new indigo.Frame(new indigo.Rectangle(0, 64, 64, 64), 250),
      ],
  ),
];

const animations = [
  new indigo.Animation(
      'traffic-lights',
      new indigo.Textured(spriteName, false),
      128,
      128,
      cycles,
  ),
];

const initialise = function(assetCollection) {
  console.log('initialise');

  console.log('Config', config);

  const startupData = {foo: 10};

  return indigo.StartUp.succeedWith(startupData);
};

const initialModel = function(startupData) {
  console.log('initialModel');
  console.log(startupData);
  return {hasRun: false};
};

const initialViewModel = function(startupData, gameModel) {
  console.log('initialViewModel');
  console.log(startupData);
  console.log(gameModel);
  return {
    num: -1,
    tint: indigo.RGBAHelper.None,
  };
};

const updateModel = function(gameTime, model, inputState, dice) {
  return function(event) {
    if (!model.hasRun) {
      console.log(gameTime.running);
      console.log('Has run? ' + model.hasRun);
      model.hasRun = true;
      console.log('Has now? ' + model.hasRun);
    }

    return indigo.OutcomeHelper.of(model);
  };
};

const updateViewModel =
  function(gameTime, model, viewModel, inputState, dice) {
    if (viewModel.num == -1) {
      console.log('Before: ' + viewModel.num);
      viewModel.num = dice.roll(6);
      console.log('After : ' + viewModel.num);
    }

    return indigo.OutcomeHelper.of(viewModel);
  };

const present = function(gameTime, model, viewModel, inputState) {
  return indigo.SceneUpdateFragmentHelper.empty
      .addGameLayerNodes(
          [
            new indigo.Text(
                'Hello, world!\nThis is some text!',
                'right',
                config.viewport.width - 10,
                20,
                1,
                0,
                1,
                1,
                fontKey,
                indigo.EffectsHelper.None.withTint(viewModel.tint),
                function(bounds, event) {
                  return [];
                },
            ),
            new indigo.Graphic(
                new indigo.Rectangle(200, 200, 256, 256),
                1,
                0,
                1,
                1,
                new indigo.Point(48, 48),
                new indigo.Rectangle(128, 0, 96, 96),
                indigo.EffectsHelper.None,
                new indigo.Textured(graphicName, false),
            ),
          ],
      );
};

indigo.Indigo.init(
    config,
    assets,
    fonts,
    animations,
    initialise,
    initialModel,
    initialViewModel,
    updateModel,
    updateViewModel,
    present,
);
