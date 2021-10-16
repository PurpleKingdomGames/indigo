/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
const React = require('react');

const CompLibrary = require('../../core/CompLibrary.js');

const MarkdownBlock = CompLibrary.MarkdownBlock; /* Used to read markdown */
const Container = CompLibrary.Container;
const GridBlock = CompLibrary.GridBlock;

class HomeSplash extends React.Component {
  render() {
    const {siteConfig, language = ''} = this.props;
    const {baseUrl, docsUrl} = siteConfig;
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
    const langPart = `${language ? `${language}/` : ''}`;
    const docUrl = doc => `${baseUrl}${docsPart}${langPart}${doc}`;

    const SplashContainer = props => (
      <div className="homeContainer">
        <div className="homeSplashFade">
          <div className="wrapper homeWrapper">{props.children}</div>
        </div>
      </div>
    );

    const Logo = props => (
      <div className="projectLogo">
        <img src={props.img_src} alt="Indigo Logo" />
      </div>
    );

    const ProjectTitle = props => (
      <h2 className="projectTitle">
        <small>{props.tagline}</small>
      </h2>
    );

    const PromoSection = props => (
      <div className="section promoSection">
        <div className="promoRow">
          <div className="pluginRowBlock">{props.children}</div>
        </div>
      </div>
    );

    const Button = props => (
      <div className="pluginWrapper buttonWrapper">
        <a className="button" href={props.href} target={props.target}>
          {props.children}
        </a>
      </div>
    );

    const pageUrl = page => baseUrl + (language ? `${language}/` : '') + page;

    return (
      <SplashContainer>
        <Logo img_src={`${baseUrl}img/indigo_logo_full.svg`} />
        <div className="inner">
          <ProjectTitle tagline={siteConfig.tagline} title={siteConfig.title} />
          <PromoSection>
            <Button href={docUrl('quickstart/hello-indigo')}>Let's build a game</Button>
            <Button href={pageUrl('snake.html')}>Quick game of snake?</Button>
            <Button href={pageUrl('pirate.html')}>Visit The Cursed Pirate</Button>
          </PromoSection>
        </div>
      </SplashContainer>
    );
  }
}

class Index extends React.Component {
  render() {
    const {config: siteConfig, language = ''} = this.props;
    const {baseUrl} = siteConfig;

    const Block = props => (
      <Container
        padding={['bottom', 'top']}
        id={props.id}
        background={props.background}>
        <GridBlock
          align="center"
          contents={props.children}
          layout={props.layout}
        />
      </Container>
    );

    const Features = () => (
      <Block layout="threeColumn">
        {[
          {
            content: 'If you can write Scala, you can make games. Indigo is powered by Scala.js, and builds with sbt & Mill.',
            image: `${baseUrl}img/site_scala_logo.png`,
            imageAlign: 'top',
            title: 'Nothing but Scala.',
          },
          {
            content: 'Confident development using Scala\'s advanced type system, purely functional APIs, and a deterministic game loop.',
            image: `${baseUrl}img/site_pirate.png`,
            imageAlign: 'top',
            title: 'Easy to Type. Easy to Test.',
          },
          {
            content: 'Indigo was designed for crisp, modern, beautiful pixel art. You can make non-pixel art games too!',
            image: `${baseUrl}img/site_consoles.png`,
            imageAlign: 'top',
            title: 'Big Beautiful Pixels.',
          },
        ]}
      </Block>
    );

    const Description = () => (
      <div className="container paddingBottom paddingTop descriptionContainer" style={{ backgroundColor: '#8749C4' }}>
        <div className="wrapper" style={{ textAlign: 'left' }}>
          <div className="gridBlock">
            <div className="blockElement alignLeft imageAlignTop twoByGridBlock">
              <h2>Let's have some fun!</h2>
              <MarkdownBlock>
              Indigo is a game engine for functional programmers. People who know how to code and want to make games _by writing code_. Just for the fun of it!
              </MarkdownBlock>
              <MarkdownBlock>
              The engine has been designed specifically with programmers in mind. It focuses on developer productivity and ease of testing.
              </MarkdownBlock>
              <MarkdownBlock>
              Indigo is written in [**Scala**](https://www.scala-lang.org/) (powered by [**Scala.js**](https://www.scala-js.org/)), as are the amazing games you'll make. It's intended for the people who _really_ like pure functions, but being Scala, allows you too flip back to a more procedural or object oriented styles if that suits you or your game better.
              </MarkdownBlock>
            </div>
            <div className="blockElement alignLeft imageAlignTop twoByGridBlock">
            <h2>&nbsp;</h2>
              <MarkdownBlock>
              Building games in Indigo is just like working any other Scala project, we've worked hard to keep the surprises to a minimum. You can even use all your favourite Scala.js compatible libraries and build tools, with extra support for SBT and Mill built in.
              </MarkdownBlock>
              <MarkdownBlock>
              Indigo is free to use and the engine is open source. There are no limits on its use, or fees or royalties to pay, ever.
              </MarkdownBlock>
              <MarkdownBlock>
              We can't wait to see what you're going to build!
              </MarkdownBlock>
            </div>
          </div>
        </div>
      </div>
    );

    const GetInvolved = () => (
      <div className="container paddingBottom paddingTop descriptionContainer" style={{ backgroundColor: '#29016A' }}>
        <div className="wrapper" style={{ textAlign: 'left' }}>
          <div className="gridBlock">
            <div className="blockElement alignLeft imageAlignTop threeByGridBlock lightLink" style={{ backgroundColor: '#0F033A', borderRadius: '20px', paddingLeft: '30px', paddingRight: '30px' , paddingBottom: '10px' }}>
              <h3>Who makes this?</h3>
              <MarkdownBlock>
                Purple Kingdom Games is made up of two long time collaborators that like building software together. They are both called Dave.
              </MarkdownBlock>
              <MarkdownBlock>
                Although the bulk of the early work on Indigo came from the Dave's, since launch we've also gratefully recieved a number of wonderful community contributions too!
              </MarkdownBlock>
            </div>
            <div className="blockElement alignLeft imageAlignTop threeByGridBlock lightLink" style={{ backgroundColor: '#0F033A', borderRadius: '20px', paddingLeft: '30px', paddingRight: '30px' , paddingBottom: '10px' }}>
              <h3>You can contribute!</h3>
              <MarkdownBlock>
                We'd love you to get involved! You could...
              </MarkdownBlock>
              <ul>
                <li><MarkdownBlock>Build a new [feature](https://github.com/PurpleKingdomGames/indigo/blob/master/CONTRIBUTING.md)</MarkdownBlock></li>
                <li><MarkdownBlock>Report an [issue](https://github.com/PurpleKingdomGames/indigo/issues)</MarkdownBlock></li>
                <li><MarkdownBlock>Help with the [documentation](https://github.com/PurpleKingdomGames/indigo-website)</MarkdownBlock></li>
              </ul>
              <MarkdownBlock>If you're not sure where to start or would like to discuss your ideas, come and say hello on [Discord](https://discord.gg/b5CD47g), or post a question on [GitHub Discussions](https://github.com/PurpleKingdomGames/indigo/discussions).</MarkdownBlock>
            </div>
            <div className="blockElement alignLeft imageAlignTop threeByGridBlock lightLink" style={{ backgroundColor: '#0F033A', borderRadius: '20px', paddingLeft: '30px', paddingRight: '30px' , paddingBottom: '10px' }}>
              <h3>Please sponsor our work!</h3>
              <MarkdownBlock>
                Indigo is a passion project, and in software, passion projects come from coffee and biscuits and ...from the ability to cover some less exciting running costs.
              </MarkdownBlock>
              <MarkdownBlock>
                If you like what we're doing and would like to help fuel Indigo's development, please consider sponsoring us on either [GitHub](https://github.com/sponsors/PurpleKingdomGames) or [Patreon](https://www.patreon.com/indigoengine).
              </MarkdownBlock>
            </div>
          </div>
        </div>
      </div>
    );

    return (
      <div>
        <HomeSplash siteConfig={siteConfig} language={language} />
        <div className="mainContainer">
          <Features />
          <Description />
          <GetInvolved />
        </div>
      </div>
    );
  }
}

module.exports = Index;
