
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
          <Game snakegame_script_path={`${baseUrl}scripts/pirategame.js`} gamepage_script_path={`${baseUrl}scripts/gamepage.js`} />
          <div align="center">
            For more information, please visit the <a href="https://github.com/PurpleKingdomGames/indigo-examples">examples repo</a>.<br />
            Requirements: Keyboard or PS4 controller, runs at fixed resolution of 1280x720.<br />
            Keyboard: Left & right arrow keys to move. Up arrow or Spacebar to jump.<br />
            PS4 Controllor: Left analog to move and 'X' button to jump.
          </div>
        </Container>
      </div>
    );
  }
};

module.exports = Snake;
