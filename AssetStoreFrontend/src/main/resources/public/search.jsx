'use strict';

const {
    useState,
} = React

const {
    TextField,
    Grid,
    Button,
    ThemeProvider,
    CssBaseline,
    Container,
    Divider,
    makeStyles,
    createStyles,
    createTheme
} = MaterialUI

const useStyles = makeStyles(() => createStyles({
    root: {
        width: '100%',
        height: '100%',
        margin: 0
    },
    gridSlot: {
        display: 'flex',
        justifyContent: 'flex-end',
        alignContent: 'flex-end',
    },
    gridAligned: {
        width: '100%',
    },
    thumb: {
        width: '100%',
        aspectRatio: 1,
        objectFit: 'cover'
    },
    resultsGrid: {
        display: 'flex',
        alignContent: 'center',
        spacing: 16,
        flexWrap: 'wrap'
    },
    resultsItem: {
        width: 160,
        aspectRatio: 1,
        margin: 8
    }
}))

const theme = createTheme({
    palette: {
        type: 'dark'
    }
})

class NoSearchResults {
    searchEnabled() {
        return true
    }

    renderSearchResults() {
        return <div>please perform a search above</div>
    }
}

class ExecutingSearch {
    searchEnabled() {
        return false
    }

    renderSearchResults() {
        return <div>executing search</div>
    }
}

class FoundSomething {
    constructor(results) {
        this.results = results
    }

    searchEnabled() {
        return true
    }

    renderSearchResults(classes) {
        return <div id="search-results-loaded" className={classes.resultsGrid}>
            {this.results.found.map(item => <div className={classes.resultsItem}>
                    <img src={item.thumb256} alt='search result item' id={item.id} className={classes.thumb} />
                </div>
            )}
        </div>
    }
}

class FoundNothing {
    searchEnabled() {
        return true
    }

    renderSearchResults() {
        return <div>nothing found, try a different search string</div>
    }
}

const SearchApplication = () => {
    const [searchText, setSearchText] = useState('')
    const [searchState, setSearchState] = useState(new NoSearchResults())
    const classes = useStyles()

    async function startSearching() {
        setSearchState(new ExecutingSearch())
        try {
            const response = await fetch(searchServiceUrl + '/search?q=' + encodeURIComponent(searchText), {method: 'post'})
            console.log("search finished")
            if (!response.ok)
                throw 'failed'

            console.log("search succeeded")
            const results = await response.json()
            console.log(results)

            if (!results.found || results.found.length < 1)
                throw 'nothing found'

            console.log("found results")

            setSearchState(new FoundSomething(results))
        } catch (ex) {
            setSearchState(new FoundNothing())
        }
    }

    return <Grid spacing={4} container direction="column">
        <Grid item>
            <Container component="span" disableGutters maxWidth="md">
                <Grid container spacing={2} direction="column">
                    <Grid item>
                        <h1>
                            AssetCo Search
                        </h1>
                    </Grid>
                    <Grid item>
                        <Grid container direction="row" justifyContent="center" spacing={2}
                              className={classes.gridAligned}>
                            <Grid item xs={10} className={classes.gridSlot}>
                                <TextField
                                    label='Enter Search Term'
                                    id='search-text'
                                    color="primary"
                                    onChange={e => setSearchText(e.target.value)}
                                    className={classes.gridAligned}
                                />
                            </Grid>
                            <Grid item xs={2} className={classes.gridSlot}>
                                <Button
                                    id="start-search-button"
                                    color="primary"
                                    variant="contained"
                                    className={classes.gridAligned}
                                    disabled={!searchState.searchEnabled()}
                                    onClick={
                                        () => {
                                            startSearching()
                                        }
                                    }
                                >
                                    GO
                                </Button>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </Container>
        </Grid>

        <Grid item>
            <Divider variant="fullWidth"/>
        </Grid>

        <Grid item>
            <Container maxWidth="lg">
                {searchState.renderSearchResults(classes)}
            </Container>
        </Grid>
    </Grid>
};

const domContainer = document.querySelector('#page');
ReactDOM.render(
    <ThemeProvider theme={theme}>
        <CssBaseline/>
        <SearchApplication/>
    </ThemeProvider>
    , domContainer);

