
const React = require('react');

const CompLibrary = require('../../core/CompLibrary.js');

const Container = CompLibrary.Container;


class Snake extends React.Component {
  render() {
    const {config: siteConfig, language = ''} = this.props;
    const {baseUrl} = siteConfig;

    const Game = props => (
      <div className="gameContainer" >
        <div id="indigo-container" align="center"></div>
        <script type="text/javascript" src={props.gamepage_script_path}></script>
        <script type="text/javascript" src={props.snakegame_script_path}></script>
        <script type="text/javascript">
          IndigoGame.launch();
        </script>
      </div>
    );

    return (
      <div className="docMainWrapper wrapper">
        <Container className="mainContainer documentContainer postContainer">
          <Game snakegame_script_path={`${baseUrl}scripts/snakegame.js`} gamepage_script_path={`${baseUrl}scripts/gamepage.js`} />
          <div align="center">
            For more information, please visit the <a href="https://github.com/PurpleKingdomGames/indigo-examples">examples repo</a>.<br />
            Keyboard controls: Use Space bar and arrows keys to navigate and accept menus.<br />
            Game controls are either left and right arrow keys to tunr left and right relative to the snake,<br />or press the arrow to change to the corresponding direction.<br />
          </div>
        </Container>
      </div>
    );
  }
};

module.exports = Snake;
